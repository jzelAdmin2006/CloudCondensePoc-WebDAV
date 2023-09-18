package tech.bison.trainee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CloudCondensePocApplication {
  public static final String WEBDAV_URL = System.getenv("WEBDAV_URL");
  public static final String WEBDAV_USERNAME = System.getenv("WEBDAV_USERNAME");
  public static final String WEBDAV_PASSWORD = System.getenv("WEBDAV_PASSWORD");

  public static void main(String[] args) {
    SpringApplication.run(CloudCondensePocApplication.class, args);
  }

}
