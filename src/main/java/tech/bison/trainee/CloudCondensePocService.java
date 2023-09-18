package tech.bison.trainee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import tech.bison.trainee.config.ArchiveConfig;
import tech.bison.trainee.config.WebDavConfig;

@Service
public class CloudCondensePocService {
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
        System.out.println("Data older than %s days archived.".formatted(archiveDayAge));
      } catch (IOException e) {
        System.out.println("Archiving failed.");
        e.printStackTrace();
      }
    });
  }

  private void archiveDataOlderThanDays(int days) throws IOException {
    final Sardine sardine = SardineFactory.begin(webDavConfig.getUsername(), webDavConfig.getPassword());
    final List<DavResource> resources = sardine.list(webDavConfig.getUrl());

    final File destDir = new File(archiveConfig.getTmpWorkDir());
    for (DavResource resource : resources) {
      if (resource.getModified()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
          .isBefore(LocalDateTime.now().minusDays(days))) {
        final String resourceUrl = webDavConfig.getUrl() + "/" + resource.getName();
        try (InputStream is = sardine.get(resourceUrl)) {
          final File targetFile = new File(destDir, resource.getName());
          FileUtils.copyInputStreamToFile(is, targetFile);
        }
      }
    }
  }
}
