/*
 * Copyright (c) 2019 - 2019 Red Hat, Inc.
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
package org.candlepin.subscriptions.a_ashared.config;

import org.candlepin.subscriptions.a_publicapi.OptInController;
import org.candlepin.subscriptions.a_worker.util.ApplicationClock;
import org.candlepin.subscriptions.a_worker.repository.AccountConfigRepository;
import org.candlepin.subscriptions.a_worker.repository.OrgConfigRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that provides admin JMX beans for tally and other functions.
 */
@Configuration
public class JmxBeansConfiguration {
    /* Define the opt-in controller in case we're running in a profile that doesn't define it */
    @Bean
    @ConditionalOnMissingBean(OptInController.class)
    OptInController optInController(ApplicationClock clock, AccountConfigRepository accountConfigRepo,
        OrgConfigRepository orgConfigRepo) {
        return new OptInController(clock, accountConfigRepo, orgConfigRepo);
    }
}
