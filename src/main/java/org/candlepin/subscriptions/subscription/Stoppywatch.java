/*
 * Copyright Red Hat, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Red Hat trademarks are not licensed under GPLv3. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.subscriptions.subscription;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/** Rather than introduce a new dependency, this'll log performance stats. */
public final class Stoppywatch implements AutoCloseable {

  private static final ThreadLocal<Stoppywatch> activeWatch = ThreadLocal.withInitial(() -> null);

  private final Stoppywatch parent;
  private final Logger log;
  private final long startNs;
  private long endNs = -1; // See notes in close().
  private final String name;
  private final List<Stoppywatch> children = new ArrayList<>();
  private boolean createChildOnSplit;
  //    private Stoppywatch activeChild; // active child is only needed because of splits

  private Stoppywatch(
      Stoppywatch parent, Logger log, String name, long startNs, boolean createChildOnSplit) {
    // Null parent means THIS instance is the root.
    this.parent = parent;
    this.log = log;
    this.startNs = startNs;
    this.name = name;
    // If createChildOnSplit is true, then when a split happens, we create a child. If it is
    // false, when a split happens, we close this watch and create a new one in its place.
    this.createChildOnSplit = createChildOnSplit;

    // Make this watch be the active watch, so that if any watches get created on this thread
    // during this watch's lifetime, they will become child watches with this as a parent (or
    // grandparent).
    activeWatch.set(this);
  }

  /**
   * Starts a new Stoppywatch if one is already active in the thread, otherwise adds a
   * sub-Stoppywatch.
   *
   * @param log Used to log the start (and the end, if it is the owning watch
   * @param name The name of the watch segment.
   * @return The Stoppywatch
   */
  public static Stoppywatch elapse(Logger log, String name) {
    Stoppywatch active = activeWatch.get();
    return new Stoppywatch(active, log, name, System.nanoTime(), true);
  }

  /**
   * Creates a split in a watch. Specifically, if this is the first split after creating a watch,
   * then a new section is named (start) and is added, and then a section with this name is given.
   * If this is a split after a previous split, then the old section is completed and a new section
   * is named and added. If this is the first split after a sibling watch was completed, then a
   * section (gap) is added... man all this sounds silly.
   *
   * <p>This was made a static rather than a regular method because otherwise it becomes possible to
   * "entangle" the children, by keeping a reference to the parent watch, and splitting while the
   * child watch is still active. This could also be avoided by looking to see if activeChild is
   * non-null, and if so, recursing until there are no active child, and splitting there, but nope,
   * not going to do that.
   *
   * @param name
   * @return
   */
  public static Stoppywatch split(String name) {
    Stoppywatch active = activeWatch.get();
    // If there's currently no watch running, then we're not tracing. Leave.
    if (active == null) {
      return null;
    }

    // Complete the previous split, if any.
    if (active.createChildOnSplit == false) {
      active.close();
      active = new Stoppywatch(active.parent, active.parent.log, name, System.nanoTime(), false);
    } else {
      // Otherwise, this is the first split, so push a child to be the active watch now.
      active = new Stoppywatch(active, active.log, name, System.nanoTime(), false);
      activeWatch.set(active);
    }

    return active;
  }

  @Override
  public void close() {
    // If the watch has already closed, do nothing.
    if (endNs != -1) {
      return;
    }

    // Mark the end time.
    endNs = System.nanoTime();

    // First, there may be an un-closed "active split" child. Check for it and close it first.
    // (We know if that is the case because active will match the current watch if there is
    // no "active split")
    if (activeWatch.get() != this) {
      // This if-statement could become a while-statement, I suppose, to be robust against
      // even "normal" watch children that weren't properly closed.
      activeWatch.get().close();
    }

    // If this is the root watch, then output the timings.
    if (parent == null) {
      log.info("Stoppywatch:\n{}", outputWatch(new StringBuilder(), this, -startNs, 0));
    } else {
      // If child, then don't log, but add the stopwatch to the parent's list of chilren.
      parent.children.add(this);
    }

    // This watch is no longer active, so control should go back to the parent (or null).
    activeWatch.set(parent);
  }

  private static StringBuilder outputWatch(
      StringBuilder sb, Stoppywatch watch, long offsetNs, int level) {
    // Write in cronological order. Notes:
    // - It is possible to have no children.

    String indent = "    ".repeat(level);
    // First, print this watch information.
    // We want the entries to be 0-based on the start time of the watch, hence offsetNs.
    long prevNs = watch.startNs + offsetNs;
    long endNs = watch.endNs + offsetNs;
    outputLine(sb, indent, watch.name, prevNs, endNs);

    // Next print children.
    watch.children.forEach(
        child -> {
          outputWatch(sb, child, offsetNs, level + 1);
        });
    return sb;

    /* Example:
    |    1000.000000ms Race (0ns to 1000ns)
        |    200.000000ms Lap1 (0ns to 200000000ns)
        |    200.000000ms Lap2 (200000000ns to 400000000ns)
        |    400.000000ms Lap3 (400000000ns to 800000000ns)
            |   200.000000ms Pit stop (500000000ns to 700000000ns)
        |    200.000000ms Final Lap (800000000ns to 1000000000ns)
     */
  }

  private static void outputLine(
      StringBuilder sb, String indent, String name, long startNs, long endNs) {
    double elapsedMs = ((double) endNs - (double) startNs) / 1_000_000d;
    sb.append(
        String.format("%s|%14.6fms %s (%dns to %dns)%n", indent, elapsedMs, name, startNs, endNs));
  }
}
