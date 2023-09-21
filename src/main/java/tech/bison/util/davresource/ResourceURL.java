package tech.bison.util.davresource;

import static java.util.Objects.requireNonNull;

import com.github.sardine.DavResource;

import okhttp3.HttpUrl;

public class ResourceURL {
  private final String baseUrl;
  private final DavResource resource;

  public ResourceURL(String baseUrl, DavResource resource) {
    this.baseUrl = baseUrl;
    this.resource = resource;
  }

  public String toStringNoTrailingSlash() {
    final HttpUrl baseHttpUrl = requireNonNull(HttpUrl.parse(baseUrl), "URL is invalid");
    final String fullPath = resource.getHref().getPath();
    final String resolvedUrl = baseHttpUrl.resolve(fullPath).toString();
    return resolvedUrl.endsWith("/") ? resolvedUrl.substring(0, resolvedUrl.length() - 1) : resolvedUrl;
  }

  public String toStringTrailingSlash() {
    return toStringNoTrailingSlash() + "/";
  }
}
