package tech.bison.trainee.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface CloudStorage {
  List<CloudResource> listResources(String url) throws IOException;

  void putResource(String url, InputStream data) throws IOException;

  void deleteResource(String url) throws IOException;

  InputStream getResource(String url) throws IOException;
}
