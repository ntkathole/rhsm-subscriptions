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
package org.candlepin.subscriptions.security;

import static org.candlepin.subscriptions.security.SecurityConfig.SECURITY_STACKTRACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.StringUtils;

/**
 * Spring Security filter responsible for pulling the Pre Shared Key out of the x-rh-swatch-psk
 * header.
 */
public class PskHeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
  private static final Logger log = LoggerFactory.getLogger(PskHeaderAuthenticationFilter.class);
  public static final String RH_PSK_HEADER = "x-rh-swatch-psk";

  private final ObjectMapper mapper;

  private AuthProperties authProps;

  private Map<String, String> pskAppMap;

  public PskHeaderAuthenticationFilter(ObjectMapper mapper, AuthProperties authProps) {
    this.mapper = mapper;
    this.authProps = authProps;
    this.pskAppMap = createPskAppMap();
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    System.out.println("PSK Filter");
    String pskHeader = request.getHeader(RH_PSK_HEADER);

    // If the header is missing it will be passed down the chain.
    if (!StringUtils.hasText(pskHeader)) {
      log.debug("{} is empty", RH_PSK_HEADER);
      return null;
    }

    try {
      var psk = new String(Base64.getDecoder().decode(pskHeader));
      var clientName = pskAppMap.get(psk);
      return clientName;
    } catch (Exception e) {
      return null;
    }
  }

  private Map<String, String> createPskAppMap() {
    Map<String, String> pskMap = new HashMap();
    var swatchPsks = new String(Base64.getDecoder().decode(authProps.getSwatchPsks()));
    System.out.println(swatchPsks);
    if (StringUtils.hasText(swatchPsks)) {
      JSONObject psksJson = new JSONObject(swatchPsks);
      psksJson.keySet().forEach(appName -> pskMap.put(psksJson.getString(appName), appName));
    }
    return pskMap;
  }

  /**
   * Credentials are not applicable in this case, so we return a dummy value.
   *
   * @param request the servlet request
   * @return a dummy value
   */
  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }
}
