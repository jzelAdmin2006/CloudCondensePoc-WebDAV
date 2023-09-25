package tech.bison.trainee.service;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import tech.bison.trainee.storage.CloudResource;
import tech.bison.trainee.storage.CloudStorage;

public class ResourceService {
  private final CloudStorage storage;

  public ResourceService(CloudStorage storage) {
    this.storage = storage;
  }

  public List<CloudResource> listResources(String url) throws IOException {
    return storage.listResources(url);
  }

  public void copyResourceToFile(CloudResource resource, File target) throws IOException {
    try (InputStream is = storage.getResource(resource.getHref())) {
      copyInputStreamToFile(is, target);
    }
  }

  public void copyResourceToFolder(CloudResource resource, File target) throws IOException {
    if (!target.exists()) {
      target.mkdirs();
    }

    List<CloudResource> resourcesInFolder = listResources(resource.getHref());
    for (CloudResource nestedResource : resourcesInFolder) {
      copyResourceToFile(nestedResource, new File(target, nestedResource.getName()));
    }
  }
}
