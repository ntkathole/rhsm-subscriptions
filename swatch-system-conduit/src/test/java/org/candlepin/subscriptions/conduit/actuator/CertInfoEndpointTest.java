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
package org.candlepin.subscriptions.conduit.actuator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.io.Resources;
import java.util.Map;
import org.candlepin.subscriptions.conduit.rhsm.client.RhsmApiProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertInfoEndpointTest {
  public static final String STORE_PASSWORD = "password";

  private RhsmApiProperties config;

  @BeforeEach
  private void setUp() {
    config = new RhsmApiProperties();
    config.setKeystoreFile(Resources.getResource("client.jks").getPath());
    config.setKeystorePassword(STORE_PASSWORD);
    config.setTruststoreFile(Resources.getResource("test-ca.jks").getPath());
    config.setTruststorePassword(STORE_PASSWORD);
  }

  @Test
  void loadStoreInfo() throws Exception {
    CertInfoEndpoint endpoint = new CertInfoEndpoint(config);
    Map<String, Map<String, String>> infoMap =
        endpoint.loadStoreInfo(
            config.getKeystoreStream(), config.getKeystorePassword().toCharArray());

    assertThat(infoMap, Matchers.hasKey("client"));

    Map<String, String> certInfo = infoMap.get("client");
    assertAll(
        "Certificate",
        () -> assertEquals("CN=Client", certInfo.get("Distinguished Name")),
        () ->
            assertEquals(
                "10638490788820008774", certInfo.get("Serial Number")),
        () ->
            assertEquals(
                "F3B39C8CAB3D8A94318727317B63E961FB9D9B0D",
                certInfo.get("SHA-1 Fingerprint").toUpperCase()),
        () -> assertEquals("CN=Test CA", certInfo.get("Issuer Distinguished Name")));
  }
}
