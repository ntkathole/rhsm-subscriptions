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
package org.candlepin.subscriptions.files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.candlepin.subscriptions.ApplicationProperties;
import org.candlepin.subscriptions.capacity.files.ProductDenylist;
import org.candlepin.subscriptions.util.ApplicationClock;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResourceLoader;

class ProductDenylistTest {

  @Test
  void testUnspecifiedLocationAllowsArbitraryProducts() throws IOException {
    ProductDenylist denylist = initProductDenylist("");
    assertFalse(denylist.productIdMatches("whee!"));
  }

  @Test
  void testAllowsProductsUnSpecifiedInDenylist() throws IOException {
    ProductDenylist denylist = initProductDenylist("classpath:item_per_line.txt");
    assertTrue(denylist.productIdMatches("I1"));
    assertTrue(denylist.productIdMatches("I2"));
    assertTrue(denylist.productIdMatches("I3"));
    assertFalse(denylist.productIdMatches("I111"));
    assertFalse(denylist.productIdMatches("I112"));
  }

  private ProductDenylist initProductDenylist(String resourceLocation) {
    ApplicationProperties props = new ApplicationProperties();
    props.setProductDenylistResourceLocation(resourceLocation);
    ProductDenylist denylist = new ProductDenylist(props, new ApplicationClock());
    denylist.setResourceLoader(new FileSystemResourceLoader());
    denylist.init();
    return denylist;
  }
}