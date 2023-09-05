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
package org.candlepin.subscriptions.resource;

import com.redhat.swatch.configuration.registry.ProductId;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.BadRequestException;
import java.time.OffsetDateTime;
import org.candlepin.subscriptions.security.auth.ReportingAccessRequired;
import org.candlepin.subscriptions.utilization.api.model.*;
import org.candlepin.subscriptions.utilization.api.resources.SubscriptionsApi;
import org.springframework.stereotype.Component;

/** Subscriptions Table API implementation. */
@Component
public class SubscriptionResource implements SubscriptionsApi {

  private final SubscriptionTableController subscriptionTableController;

  public SubscriptionResource(SubscriptionTableController subscriptionTableController) {
    this.subscriptionTableController = subscriptionTableController;
  }

  @ReportingAccessRequired
  @Override
  public SkuCapacityReport getSkuCapacityReport(
      String productIdValue,
      @Min(0) Integer offset,
      @Min(1) Integer limit,
      ReportCategory category,
      ServiceLevelType sla,
      UsageType usage,
      BillingProviderType billingProvider,
      String billingAccountId,
      OffsetDateTime beginning,
      OffsetDateTime ending,
      Uom uom,
      SkuCapacityReportSort sort,
      SortDirection dir) {

    ProductId productId;
    try {
      productId = ProductId.fromString(productIdValue);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e);
    }
    return subscriptionTableController.capacityReportBySku(
        productId,
        offset,
        limit,
        category,
        sla,
        usage,
        billingProvider,
        billingAccountId,
        uom,
        sort,
        dir);
  }
}
