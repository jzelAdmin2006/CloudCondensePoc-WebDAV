package tech.bison.trainee;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudCondensePocAppConfig {

  @Bean
  ExecutorService archiveExecutor() {
    return Executors.newSingleThreadExecutor();
  }
}
