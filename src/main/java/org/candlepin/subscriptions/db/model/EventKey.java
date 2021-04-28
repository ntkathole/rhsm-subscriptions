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
package org.candlepin.subscriptions.db.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;
import org.candlepin.subscriptions.json.Event;

/**
 * Defines the unique constraints for an EventRecord. This object is primarily used for creating
 * lookup tables while processing Events pulled from the DB.
 */
@Getter
public class EventKey {

  private String accountNumber;
  private String eventType;
  private String eventSource;
  private String instanceId;
  private OffsetDateTime timestamp;

  public EventKey(
      String accountNumber,
      String eventSource,
      String eventType,
      String instanceId,
      OffsetDateTime timestamp) {
    Objects.requireNonNull(accountNumber, "EventKey requires a non null 'accountNumber'.");
    this.accountNumber = accountNumber;

    Objects.requireNonNull(eventType, "EventKey requires a non null 'eventType'.");
    this.eventType = eventType;

    Objects.requireNonNull(eventSource, "EventKey requires a non null 'eventSource'.");
    this.eventSource = eventSource;

    Objects.requireNonNull(instanceId, "EventKey requires a non null 'instanceId'.");
    this.instanceId = instanceId;

    Objects.requireNonNull(timestamp, "EventKey requires a non null 'timestamp'.");
    this.timestamp = timestamp;
  }

  public static EventKey fromEvent(Event event) {
    return new EventKey(
        event.getAccountNumber(),
        event.getEventSource(),
        event.getEventType(),
        event.getInstanceId(),
        event.getTimestamp());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventKey eventKey = (EventKey) o;
    return Objects.equals(accountNumber, eventKey.accountNumber)
        && Objects.equals(eventType, eventKey.eventType)
        && Objects.equals(eventSource, eventKey.eventSource)
        && Objects.equals(instanceId, eventKey.instanceId)
        && Objects.equals(timestamp, eventKey.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountNumber, eventType, eventSource, instanceId, timestamp);
  }
}
