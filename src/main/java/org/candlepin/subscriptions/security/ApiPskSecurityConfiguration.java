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

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.candlepin.subscriptions.rbac.RbacProperties;
import org.candlepin.subscriptions.rbac.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;

/**
 * Configuration class for Spring Security.
 *
 * <p>The architecture here can be confusing due to our use of a custom filter. Here is a discussion
 * of the Spring Security architecture:
 * https://spring.io/guides/topicals/spring-security-architecture In our case, requests are
 * pre-authenticated and the relevant information is stored in a custom header.
 *
 * <p>We add the IdentityHeaderAuthenticationFilter as a filter to the Spring Security
 * FilterChainProxy that sits in the standard servlet filter chain and delegates out to all the
 * various Spring Security filters.
 *
 * <ol>
 *   <li>IdentityHeaderAuthenticationFilter parses the servlet request to create the
 *       InsightsUserPrincipal and places it in an Authentication object.
 *   <li>IdentityHeaderAuthenticationFilter's superclass invokes the
 *       IdentityHeaderAuthenticationDetailsSource to populate the Authentication object with the
 *       PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails which contain the roles.
 *   <li>The Authentication object is passed to the Spring Security AuthenticationManager. In this
 *       case, we're using a ProviderManager with one AuthenticationProvider,
 *       IdentityHeaderAuthenticationProvider, installed.
 *   <li>The IdentityHeaderAuthenticationProvider is invoked to build a blessed Authentication
 *       object. We examine the current Authentication and make sure everything we expect is there.
 *       Then we take the granted authorities provided from the
 *       IdentityHeaderAuthenticationDetailsSource and push them and the InsightsUserPrincipal into
 *       a new, blessed Authentication object and return it.
 * </ol>
 */
@Configuration
@Order(99)
public class ApiPskSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired protected ObjectMapper mapper;
  @Autowired protected SecurityProperties secProps;
  @Autowired protected ManagementServerProperties actuatorProps;
  @Autowired protected AuthProperties authProperties;
  @Autowired protected ConfigurableEnvironment env;


  @Autowired
  public void initialize(AuthenticationManagerBuilder auth, DataSource dataSource) {
    auth.authenticationProvider(pskHeaderAuthenticationProvider());
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) {
    // Add our AuthenticationProvider to the Provider Manager's list
    auth.authenticationProvider(pskHeaderAuthenticationProvider());
  }

  public AuthenticationProvider pskHeaderAuthenticationProvider() {
    return new PskHeaderAuthenticationProvider();
  }

  // NOTE: intentionally *not* annotated w/ @Bean; @Bean causes an *extra* use as an application
  // filter
  public PskHeaderAuthenticationFilter pskHeaderAuthenticationFilter() throws Exception {
    PskHeaderAuthenticationFilter filter = new PskHeaderAuthenticationFilter(mapper, authProperties);
    filter.setCheckForPrincipalChanges(true);
    filter.setAuthenticationManager(authenticationManager());
    filter.setAuthenticationFailureHandler(new IdentityHeaderAuthenticationFailureHandler(mapper));
    filter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
    return filter;
  }

  @Bean
  public AccessDeniedHandler restInternalAccessDeniedHandler() {
    return new RestAccessDeniedHandler(mapper);
  }

  @Bean
  public AuthenticationEntryPoint restInternalAuthenticationEntryPoint() {
    return new RestAuthenticationEntryPoint(new IdentityHeaderAuthenticationFailureHandler(mapper));
  }

  // NOTE: intentionally *not* annotated with @Bean; @Bean causes an extra use as an application
  // filter
  public AntiCsrfFilter antiCsrfFilter(SecurityProperties secProps, ConfigurableEnvironment env) {
    return new AntiCsrfFilter(secProps, env);
  }

  // NOTE: intentionally *not* annotated w/ @Bean; @Bean causes an *extra* use as an application
  // filter
  public MdcFilter mdcFilter() {
    return new MdcFilter();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    String apiPath =
        env.getRequiredProperty(
            "rhsm-subscriptions.package_uri_mappings.org.candlepin.subscriptions.resteasy");
    http.addFilter(pskHeaderAuthenticationFilter())
        .addFilterAfter(mdcFilter(), PskHeaderAuthenticationFilter.class)
        .addFilterAt(antiCsrfFilter(secProps, env), CsrfFilter.class)
        .authenticationProvider(pskHeaderAuthenticationProvider())
        .csrf()
        .disable()
        .exceptionHandling()
        .accessDeniedHandler(restInternalAccessDeniedHandler())
        .authenticationEntryPoint(restInternalAuthenticationEntryPoint())
        .and()
        // disable sessions, our API is stateless, and sessions cause RBAC information to be
        // cached
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .anonymous() // Creates an anonymous user if no header is present at all. Prevents NPEs
        .and()
        .authorizeRequests()
        .antMatchers("/**")
        .permitAll()
        // Allow access to the Spring Actuator "root" which displays the available endpoints
        .requestMatchers(
            request ->
                request.getServerPort() == actuatorProps.getPort()
                    && request.getContextPath().equals(actuatorProps.getBasePath()))
        .permitAll()
        .requestMatchers(EndpointRequest.to("health", "info", "prometheus", "hawtio"))
        .permitAll()
        //Only internal endpoints should be authenticated this way
        .antMatchers(String.format("/%s/internal/**", apiPath))
        .authenticated();
  }
}
