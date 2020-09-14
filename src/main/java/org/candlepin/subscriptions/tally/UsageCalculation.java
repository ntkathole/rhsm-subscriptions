/*
 * Copyright (c) 2009 - 2019 Red Hat, Inc.
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
package org.candlepin.subscriptions.tally;

import org.candlepin.subscriptions.db.model.HardwareMeasurementType;
import org.candlepin.subscriptions.db.model.ServiceLevel;
import org.candlepin.subscriptions.db.model.TallySnapshot;
import org.candlepin.subscriptions.db.model.Usage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * The calculated usage for a key where key is (productId, sla).
 */
public class UsageCalculation {

    private final Key key;

    /**
     * Natural key for a given calculation.
     *
     * Note that already data is scoped to an account, so account is not included in the key.
     */
    public static class Key {
        private final String productId;
        private final ServiceLevel sla;
        private final Usage usage;

        public Key(String productId, ServiceLevel sla, Usage usage) {
            this.productId = productId;
            this.sla = sla;
            this.usage = usage;
        }

        public String getProductId() {
            return productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key that = (Key) o;
            return Objects.equals(productId, that.productId) &&
                Objects.equals(sla, that.sla) &&
                Objects.equals(usage, that.usage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, sla, usage);
        }

        public static Key fromTallySnapshot(TallySnapshot snapshot) {
            return new Key(snapshot.getProductId(), snapshot.getServiceLevel(), snapshot.getUsage());
        }

        @Override
        public String toString() {
            return "Key{" +
                "productId='" + productId + '\'' +
                ", sla=" + sla +
                ", usage=" + usage + '}';
        }
    }

    /**
     * Provides metric totals associated with each hardware type associated with a calculation.
     */
    public class Totals {
        private int cores;
        private int sockets;
        private int instances;

        public Totals() {
            cores = 0;
            sockets = 0;
            instances = 0;
        }

        public String toString() {
            return String.format("[cores: %s, sockets: %s, instances: %s]", cores, sockets, instances);
        }

        public int getCores() {
            return cores;
        }

        public int getSockets() {
            return sockets;
        }

        public int getInstances() {
            return instances;
        }
    }

    private Map<HardwareMeasurementType, Totals> mappedTotals;

    public UsageCalculation(Key key) {
        this.key = key;
        this.mappedTotals = new EnumMap<>(HardwareMeasurementType.class);
    }

    public String getProductId() {
        return key.productId;
    }

    public ServiceLevel getSla() {
        return key.sla;
    }

    public Usage getUsage() {
        return key.usage;
    }

    public Totals getTotals(HardwareMeasurementType type) {
        return mappedTotals.get(type);
    }

    public void addPhysical(int cores, int sockets, int instances) {
        increment(HardwareMeasurementType.PHYSICAL, cores, sockets, instances);
        addToTotal(cores, sockets, instances);
    }

    public void addHypervisor(int cores, int sockets, int instances) {
        increment(HardwareMeasurementType.HYPERVISOR, cores, sockets, instances);
        addToTotal(cores, sockets, instances);
    }

    public void addVirtual(int cores, int sockets, int instances) {
        increment(HardwareMeasurementType.VIRTUAL, cores, sockets, instances);
        addToTotal(cores, sockets, instances);
    }

    public void addToTotal(int cores, int sockets, int instances) {
        increment(HardwareMeasurementType.TOTAL, cores, sockets, instances);
    }

    public void addCloudProvider(HardwareMeasurementType cloudType, int cores, int sockets, int instances) {
        if (!HardwareMeasurementType.isSupportedCloudProvider(cloudType.name())) {
            throw new IllegalArgumentException(String.format("%s is not a supported cloud provider type.",
                cloudType));
        }

        increment(cloudType, cores, sockets, instances);
        addToTotal(cores, sockets, instances);
    }

    private void increment(HardwareMeasurementType type, int cores, int sockets, int instances) {
        Totals total = getOrDefault(type);
        total.cores += cores;
        total.sockets += sockets;
        total.instances += instances;
    }

    private Totals getOrDefault(HardwareMeasurementType type) {
        this.mappedTotals.putIfAbsent(type, new Totals());
        return this.mappedTotals.get(type);
    }

    public boolean hasMeasurements() {
        return !this.mappedTotals.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("[Product: %s, sla: %s, usage: %s", key.productId, key.sla, key.usage));
        for (Entry<HardwareMeasurementType, Totals> entry : mappedTotals.entrySet()) {
            builder.append(String.format(", %s: %s", entry.getKey(), entry.getValue()));
        }
        builder.append("]");
        return builder.toString();
    }

}
