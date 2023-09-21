package tech.bison.trainee;

import static java.util.Comparator.comparingInt;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static tech.bison.util.sevenzip.SevenZip.SEVEN_ZIP_FILE_ENDING;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import tech.bison.trainee.config.ArchiveConfig;
import tech.bison.trainee.config.WebDavConfig;
import tech.bison.util.davresource.ResourceURL;
import tech.bison.util.sevenzip.SevenZip;

@Service
public class CloudCondensePocService {
  private static final Logger LOGGER = Logger.getLogger(CloudCondensePocService.class.getName());

  private final ExecutorService archiveExecutor;

  private final WebDavConfig webDavConfig;
  private final ArchiveConfig archiveConfig;

  public CloudCondensePocService(ExecutorService archiveExecutor, WebDavConfig webDavConfig,
      ArchiveConfig archiveConfig) {
    this.archiveExecutor = archiveExecutor;
    this.webDavConfig = webDavConfig;
    this.archiveConfig = archiveConfig;
  }

  public void archiveDataByRequest(int archiveDayAge) {
    archiveExecutor.submit(() -> {
      try {
        archiveDataOlderThanDays(archiveDayAge);
        LOGGER.log(Level.INFO, "Data older than %s days archived.".formatted(archiveDayAge));
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Archiving failed.");
        e.printStackTrace();
      }
    });
  }

  private void archiveDataOlderThanDays(int days) throws IOException {
    final Sardine sardine = SardineFactory.begin(webDavConfig.getUsername(), webDavConfig.getPassword());
    final List<DavResource> resources = listUnarchivedContent(sardine, webDavConfig.getUrl());

    for (DavResource resource : resources) {
      if (resource.getModified()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
          .isBefore(LocalDateTime.now().minusDays(days))) {
        archive(sardine, resource);
      }
    }
  }

  private List<DavResource> listUnarchivedContent(final Sardine sardine, final String url) throws IOException {
    return sardine.list(url)
        .stream()
        .sorted(comparingInt(r -> r.getPath().length()))
        .skip(1)
        .filter(this::isAlreadyArchived)
        .toList();
  }

  private void archive(final Sardine sardine, DavResource resource) throws IOException {
    final File tmpWorkDir = new File(archiveConfig.getTmpWorkDir());
    final File target = new File(tmpWorkDir, resource.getName());
    try {
      if (resource.isDirectory()) {
        copyResourceToFolder(sardine, resource, target);
      } else {
        copyResourceToFile(sardine, resource, target);
      }
      final File archive = new File(archiveConfig.getTmpWorkDir(), target.getName() + SEVEN_ZIP_FILE_ENDING);
      new SevenZip().compress(target, archive);
      replaceResource(sardine, resource, archive);
    } finally {
      cleanDirectory(tmpWorkDir);
    }
  }

  private void replaceResource(final Sardine sardine, DavResource resource, final File archive) throws IOException {
    final ResourceURL url = new ResourceURL(webDavConfig.getUrl(), resource);
    try (InputStream is = new FileInputStream(archive)) {
      sardine.put(url.toStringNoTrailingSlash() + SEVEN_ZIP_FILE_ENDING, is);
    }
    sardine.delete(resource.isDirectory() ? url.toStringTrailingSlash() : url.toStringNoTrailingSlash());
  }

  private void copyResourceToFolder(final Sardine sardine, DavResource resource, final File target) throws IOException {
    target.mkdirs();

    final LinkedList<DavResource> queue = new LinkedList<>();
    queue.add(resource);

    while (!queue.isEmpty()) {
      final DavResource currentResource = queue.poll();
      final List<DavResource> childResources = listUnarchivedContent(sardine,
          new ResourceURL(webDavConfig.getUrl(), currentResource).toStringNoTrailingSlash());

      for (DavResource childResource : childResources) {
        processFolderCopyQueueItems(sardine, resource, target, queue, childResource);
      }
    }
  }

  private void processFolderCopyQueueItems(final Sardine sardine, DavResource resource, final File target,
                                           final LinkedList<DavResource> queue,
                                           DavResource childResource) throws IOException {
    if (isAlreadyArchived(childResource)) {
      final String relativePath = childResource.getHref().getPath().substring(resource.getHref().getPath().length());
      final File localTarget = new File(target, relativePath);

      if (childResource.isDirectory()) {
        localTarget.mkdirs();
        queue.addAll(sardine.list(new ResourceURL(webDavConfig.getUrl(), childResource).toStringNoTrailingSlash()));
      } else {
        copyResourceToFile(sardine, childResource, localTarget);
      }
    }
  }

  private boolean isAlreadyArchived(DavResource childResource) {
    return !childResource.getName().endsWith(SEVEN_ZIP_FILE_ENDING);
  }

  private void copyResourceToFile(final Sardine sardine, DavResource resource, final File target) throws IOException {
    try (InputStream is = sardine.get(new ResourceURL(webDavConfig.getUrl(), resource).toStringNoTrailingSlash())) {
      copyInputStreamToFile(is, target);
    }
  }
}
