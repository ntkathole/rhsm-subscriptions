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
package org.candlepin.subscriptions.product;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.candlepin.subscriptions.capacity.CapacityReconciliationController;
import org.candlepin.subscriptions.resource.ResourceUtils;
import org.candlepin.subscriptions.security.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.JmxException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/** Allows syncing of offerings. */
@Profile("capacity-ingress")
@Component
@ManagedResource
@Slf4j
public class OfferingJmxBean {
  private final OfferingSyncController offeringSync;
  private final CapacityReconciliationController capacityReconciliationController;
  private final SecurityProperties properties;

  @Autowired
  public OfferingJmxBean(
      OfferingSyncController offeringSync,
      CapacityReconciliationController capacityReconciliationController,
      SecurityProperties properties) {
    this.offeringSync = offeringSync;
    this.capacityReconciliationController = capacityReconciliationController;
    this.properties = properties;
  }

  @ManagedOperation(description = "Sync an offering from the upstream source.")
  @ManagedOperationParameter(name = "sku", description = "A marketing SKU")
  public String syncOffering(String sku) {
    try {
      Object principal = ResourceUtils.getPrincipal();
      log.info("Sync for offering {} triggered over JMX by {}", sku, principal);
      SyncResult result = offeringSync.syncOffering(sku);

      return String.format("%s for offeringSku=\"%s\".", result, sku);
    } catch (Exception e) {
      log.error("Error syncing offering", e);
      throw new JmxException("Error syncing offering. See log for details.");
    }
  }

  @ManagedOperation(
      description = "Syncs all offerings listed in allow list from the upstream source.")
  public String syncAllOfferings() {
    try {
      Object principal = ResourceUtils.getPrincipal();
      log.info("Sync all offerings triggered over JMX by {}", principal);
      int numProducts = offeringSync.syncAllOfferings();

      return "Enqueued " + numProducts + " offerings to be synced.";
    } catch (RuntimeException e) {
      throw new JmxException("Error enqueueing offerings to be synced. See log for details.");
    }
  }

  @ManagedOperation(description = "Reconcile capacity for an offering from the upstream source.")
  @ManagedOperationParameter(name = "sku", description = "A marketing SKU")
  public void forceReconcileOffering(String sku) {
    try {
      Object principal = ResourceUtils.getPrincipal();
      log.info("Capacity Reconciliation for sku {} triggered over JMX by {}", sku, principal);
      capacityReconciliationController.reconcileCapacityForOffering(sku, 0, 100);
    } catch (Exception e) {
      log.error("Error reconciling offering", e);
      throw new JmxException("Error reconciling offering. See log for details.");
    }
  }

  @ManagedOperation(
      description =
          "Save offerings manually, ignoring allowlist. Supported only in dev-mode. Locally, you can insert all")
  @ManagedOperationParameter(
      name = "offeringsJsonArray",
      description =
          "JSON array containing offerings to save. (Objects are the same as seen in product-stub-data 'tree-' files).")
  @ManagedOperationParameter(
      name = "derivedSkuJsonArray",
      description =
          "JSON array containing derived SKU tree. (Objects are the same as seen in product-stub-data 'tree-' files).")
  @ManagedOperationParameter(
      name = "engProdJsonArray",
      description =
          "JSON array of endProd mappings. (Objects are the same as seen in product-stub-data 'engprods-' files)")
  @ManagedOperationParameter(
      name = "reconcileCapacity",
      description = "Invoke reconciliation logic to create capacity?")
  public void saveOfferings(
      String offeringsJsonArray,
      String derivedSkuJsonArray,
      String engProdJsonArray,
      boolean reconcileCapacity) {
    if (!properties.isDevMode() && !properties.isManualSubscriptionEditingEnabled()) {
      throw new JmxException("This feature is not currently enabled.");
    }
    try {
      Object principal = ResourceUtils.getPrincipal();
      log.info("Save of new offerings triggered over JMX by {}", principal);
      Stream<String> skus =
          offeringSync.saveOfferings(offeringsJsonArray, derivedSkuJsonArray, engProdJsonArray);
      if (reconcileCapacity) {
        skus.forEach(capacityReconciliationController::enqueueReconcileCapacityForOffering);
      }
    } catch (Exception e) {
      log.error("Error saving offerings", e);
      throw new JmxException("Error saving offerings. See log for details.");
    }
  }

  @ManagedOperation(description = "Delete an offering manually. Supported only in dev-mode.")
  public void deleteOffering(String sku) {
    if (!properties.isDevMode() && !properties.isManualSubscriptionEditingEnabled()) {
      throw new JmxException("This feature is not currently enabled.");
    }
    Object principal = ResourceUtils.getPrincipal();
    log.info("Delete of subscription triggered over JMX by {}", principal);
    offeringSync.deleteOffering(sku);
  }
}
