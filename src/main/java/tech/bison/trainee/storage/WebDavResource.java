package tech.bison.trainee.storage;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.github.sardine.DavResource;

public class WebDavResource implements CloudResource {
  private final DavResource davResource;

  public WebDavResource(DavResource davResource) {
    this.davResource = davResource;
  }

  @Override
  public String getName() {
    return davResource.getName();
  }

  @Override
  public boolean isDirectory() {
    return davResource.isDirectory();
  }

  @Override
  public LocalDateTime getLastModified() {
    return davResource.getModified().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  @Override
  public String getHref() {
    return davResource.getHref().getPath();
  }
}
