/*
 * Copyright (c) 2020 Red Hat, Inc.
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
import java.util.List;
import java.util.UUID;

public final class HostBuilder {

    private UUID id;
    private String inventoryId;
    private String insightsId;
    private String displayName;
    private String accountNumber;
    private String orgId;
    private String subscriptionManagerId;
    private Integer cores;
    private Integer sockets;
    private boolean guest;
    private String hypervisorUuid;
    private HostHardwareType hardwareType;
    private Integer numOfGuests;
    private OffsetDateTime lastSeen;
    private List<HostTallyBucket> buckets;
    private String cloudProvider;

    private HostBuilder() {
    }

    public static HostBuilder aHost() {
        return new HostBuilder();
    }

    public HostBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public HostBuilder withInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
        return this;
    }

    public HostBuilder withInsightsId(String insightsId) {
        this.insightsId = insightsId;
        return this;
    }

    public HostBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public HostBuilder withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public HostBuilder withOrgId(String orgId) {
        this.orgId = orgId;
        return this;
    }

    public HostBuilder withSubscriptionManagerId(String subscriptionManagerId) {
        this.subscriptionManagerId = subscriptionManagerId;
        return this;
    }

    public HostBuilder withCores(Integer cores) {
        this.cores = cores;
        return this;
    }

    public HostBuilder withSockets(Integer sockets) {
        this.sockets = sockets;
        return this;
    }

    public HostBuilder withGuest(boolean guest) {
        this.guest = guest;
        return this;
    }

    public HostBuilder withHypervisorUuid(String hypervisorUuid) {
        this.hypervisorUuid = hypervisorUuid;
        return this;
    }

    public HostBuilder withHardwareType(HostHardwareType hardwareType) {
        this.hardwareType = hardwareType;
        return this;
    }

    public HostBuilder withNumOfGuests(Integer numOfGuests) {
        this.numOfGuests = numOfGuests;
        return this;
    }

    public HostBuilder withLastSeen(OffsetDateTime lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public HostBuilder withBuckets(List<HostTallyBucket> buckets) {
        this.buckets = buckets;
        return this;
    }

    public HostBuilder withCloudProvider(String cloudProvider) {
        this.cloudProvider = cloudProvider;
        return this;
    }

    public Host build() {
        Host host = new Host();
        host.setId(id);
        host.setInventoryId(inventoryId);
        host.setInsightsId(insightsId);
        host.setDisplayName(displayName);
        host.setAccountNumber(accountNumber);
        host.setOrgId(orgId);
        host.setSubscriptionManagerId(subscriptionManagerId);
        host.setCores(cores);
        host.setSockets(sockets);
        host.setGuest(guest);
        host.setHypervisorUuid(hypervisorUuid);
        host.setHardwareType(hardwareType);
        host.setNumOfGuests(numOfGuests);
        host.setLastSeen(lastSeen);
        host.setBuckets(buckets);
        host.setCloudProvider(cloudProvider);
        return host;
    }
}
