package tech.bison.trainee.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import tech.bison.trainee.config.WebDavConfig;

public class WebDavStorage implements CloudStorage {
  private final WebDavConfig webDavConfig;
  private final Sardine sardine;

  public WebDavStorage(WebDavConfig webDavConfig) {
    this.webDavConfig = webDavConfig;
    this.sardine = SardineFactory.begin(webDavConfig.getUsername(), webDavConfig.getPassword());
  }

  @Override
  public List<CloudResource> listResources(String url) throws IOException {
    return sardine.list(url).stream().map(r -> (CloudResource) new WebDavResource(r)).toList();
  }

  @Override
  public void putResource(String url, InputStream data) throws IOException {
    sardine.put(url, data);
  }

  @Override
  public void deleteResource(String url) throws IOException {
    sardine.delete(url);
  }

  @Override
  public InputStream getResource(String url) throws IOException {
    return sardine.get(url);
  }

  /**
   * @deprecated storage config will be dynamic in the future
   */
  @Deprecated(forRemoval = true)
  public WebDavConfig getConfig() {
    return webDavConfig;
  }
}
