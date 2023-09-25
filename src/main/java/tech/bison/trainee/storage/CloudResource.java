package tech.bison.trainee.storage;

import java.time.LocalDateTime;

public interface CloudResource {
  String getName();

  boolean isDirectory();

  LocalDateTime getLastModified();

  String getHref();
}
