package tech.bison.trainee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "archive")
public class ArchiveConfig {

  private String tmpWorkDir;

  public String getTmpWorkDir() {
    return tmpWorkDir;
  }

  public void setTmpWorkDir(String tmpWorkDir) {
    this.tmpWorkDir = tmpWorkDir;
  }

}
