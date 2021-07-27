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
package org.candlepin.subscriptions.jmx;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.candlepin.subscriptions.ApplicationProperties;
import org.candlepin.subscriptions.event.EventController;
import org.candlepin.subscriptions.json.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.JmxException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * JMX Bean for interacting with the event store.
 *
 * <p>Allows insertion of event JSON *only in dev-mode*.
 */
@Component
@ManagedResource
public class EventJmxBean {
  private static final Logger log = LoggerFactory.getLogger(EventJmxBean.class);

  private final ApplicationProperties applicationProperties;
  private final EventController eventController;
  private final ObjectMapper objectMapper;

  public EventJmxBean(
      ApplicationProperties applicationProperties,
      EventController eventController,
      ObjectMapper objectMapper) {
    this.applicationProperties = applicationProperties;
    this.eventController = eventController;
    this.objectMapper = objectMapper;
  }

  @Transactional
  @ManagedOperation(description = "Fetch events (for debugging).")
  @ManagedOperationParameter(name = "accountNumber", description = "Account number")
  @ManagedOperationParameter(name = "begin", description = "Beginning of time range (inclusive)")
  @ManagedOperationParameter(name = "end", description = "End of time range (exclusive)")
  public List<Event> fetchEventsInTimeRange(String accountNumber, String begin, String end) {
    OffsetDateTime beginValue = OffsetDateTime.parse(begin);
    OffsetDateTime endValue = OffsetDateTime.parse(end);
    return eventController
        .fetchEventsInTimeRange(accountNumber, beginValue, endValue)
        .collect(Collectors.toList());
  }

  @ManagedOperation(description = "Fetch an event")
  @ManagedOperationParameter(name = "eventId", description = "Event UUID")
  public Event getEvent(String eventId) {
    Optional<Event> event = eventController.getEvent(UUID.fromString(eventId));
    return event.isPresent() ? event.get() : null;
  }

  @ManagedOperation(description = "Delete an event")
  @ManagedOperationParameter(name = "eventId", description = "Event UUID")
  public String deleteEvent(String eventId) {
    if (!applicationProperties.isDevMode()) {
      throw new UnsupportedOperationException("Can only delete events in dev mode!");
    }

    try {
      eventController.deleteEvent(UUID.fromString(eventId));
      return String.format("Successfully deleted Event with ID: %s", eventId);
    } catch (Exception e) {
      return String.format(
          "Failed to delete Event with ID: %s  Cause: %s", eventId, e.getMessage());
    }
  }

  @ManagedOperation(description = "Save an event. Supported only in dev-mode.")
  @ManagedOperationParameter(name = "json", description = "Event JSON")
  public Event saveEvent(String json) {
    if (!applicationProperties.isDevMode()) {
      throw new JmxException("Unsupported outside dev-mode!");
    }
    try {
      return eventController.saveEvent(objectMapper.readValue(json, Event.class));
    } catch (Exception e) {
      log.error("Error saving event", e);
      throw new JmxException("Error saving event. See log for details.");
    }
  }

  @ManagedOperation(description = "Save a list of events. Supported only in dev-mode.")
  @ManagedOperationParameter(
      name = "jsonListOfEvents",
      description = "Event list specified as JSON")
  public List<Event> saveEvents(String jsonListOfEvents) {
    if (!applicationProperties.isDevMode()) {
      throw new JmxException("Unsupported outside dev-mode!");
    }
    try {
      return eventController.saveAll(
          objectMapper.readValue(
              jsonListOfEvents,
              objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class)));
    } catch (Exception e) {
      log.error("Error saving events", e);
      throw new JmxException("Error saving event. See log for details.");
    }
  }
}
