package org.candlepin.subscriptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.time.ZoneOffset;
import java.util.TimeZone;

@SpringBootApplication
public class SwatchApiApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    SpringApplication app = new SpringApplication(SwatchApiApplication.class);
    app.run(args);
  }
}
