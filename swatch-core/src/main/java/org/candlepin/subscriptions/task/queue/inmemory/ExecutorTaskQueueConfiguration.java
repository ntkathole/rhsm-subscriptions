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
package org.candlepin.subscriptions.task.queue.inmemory;

import org.candlepin.subscriptions.task.queue.TaskConsumerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration that is common to both producers and consumers for in-memory task queues.
 *
 * <p>Only activated as a fallback (in case Kafka is disabled).
 */
@Configuration
@Profile("!kafka-queue")
public class ExecutorTaskQueueConfiguration {
  private static final Logger log = LoggerFactory.getLogger(ExecutorTaskQueueConfiguration.class);

  /**
   * Creates an in-memory queue, implemented with {@link java.util.concurrent.ThreadPoolExecutor}.
   *
   * <p>Does not block while executing a task. Spin up a new thread for each task, only practically
   * bound by amount of memory available.
   *
   * @see TaskConsumerConfiguration
   */
  @Bean
  ExecutorTaskQueue inMemoryQueue() {
    log.info("Configuring an in-memory task queue.");
    return new ExecutorTaskQueue();
  }
}
