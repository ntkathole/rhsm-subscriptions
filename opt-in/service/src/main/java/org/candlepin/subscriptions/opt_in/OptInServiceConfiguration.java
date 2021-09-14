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
package org.candlepin.subscriptions.opt_in;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import javax.validation.Validator;
import org.candlepin.subscriptions.util.ApplicationClock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Class to hold configuration beans common to all profiles and import all profile configurations
 */
@Configuration
@EnableConfigurationProperties
public class OptInServiceConfiguration implements WebMvcConfigurer {
  @Bean
  OptInServiceProperties applicationProperties() {
    return new OptInServiceProperties();
  }

  @Bean
  @Primary
  ObjectMapper objectMapper(OptInServiceProperties properties) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    objectMapper.configure(
        SerializationFeature.INDENT_OUTPUT, properties.isPrettyPrintJson());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());

    // Explicitly load the modules we need rather than use ObjectMapper.findAndRegisterModules in
    // order to avoid com.fasterxml.jackson.module.scala.DefaultScalaModule, which was causing
    // deserialization to ignore @JsonProperty on OpenApi classes.
    objectMapper.registerModule(new JaxbAnnotationModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());

    return objectMapper;
  }

  /* Do not declare a MethodValidationPostProcessor!
   *
   * The Spring Core documents instruct the user to create a MethodValidationPostProcessor in order to
   * enable method validation.  However, Spring Boot takes care of creating that bean that itself:
   * "The method validation feature supported by Bean Validation 1.1 is automatically enabled as long as a
   * JSR-303 implementation (such as Hibernate validator) is on the classpath" (from
   * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-validation).
   *
   * Creating our own MethodValidationPostProcessor causes ConstraintValidator implementations to *not*
   * receive injection from the Spring IoC container.
   */

  @Bean
  public Validator validator() {
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  @Bean
  public ApplicationClock applicationClock() {
    return new ApplicationClock();
  }
}
