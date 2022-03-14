package org.candlepin.subscriptions.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rhsm-subscriptions.auth")
public class AuthProperties {

  /** JSON of a Pre Shared Key (PSK) list used in service-to-service auth. */
  private String swatchPsks;

}
