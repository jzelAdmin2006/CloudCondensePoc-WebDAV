package tech.bison.trainee;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import okhttp3.HttpUrl;
import tech.bison.trainee.config.ArchiveConfig;
import tech.bison.trainee.config.WebDavConfig;

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
    final List<DavResource> resources = sardine.list(webDavConfig.getUrl())
        .stream()
        .sorted(Comparator.comparingInt(r -> r.getPath().length()))
        .skip(1)
        .toList();

    for (DavResource resource : resources) {
      if (resource.getModified()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
          .isBefore(LocalDateTime.now().minusDays(days))) {
        try (InputStream is = sardine.get(toUrl(resource))) {
          final File targetFile = new File(new File(archiveConfig.getTmpWorkDir()), resource.getName());
          copyInputStreamToFile(is, targetFile);
        }
      }
    }
  }

  private String toUrl(DavResource resource) {
    return requireNonNull(HttpUrl.parse(webDavConfig.getUrl()), "URL is invalid").newBuilder()
        .addPathSegment(resource.getName())
        .build()
        .toString();
  }
}
