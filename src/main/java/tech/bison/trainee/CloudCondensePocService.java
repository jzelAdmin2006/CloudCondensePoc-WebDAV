package tech.bison.trainee;

import static tech.bison.trainee.CloudCondensePocApplication.TMP_ARCHIVE_WORK_DIR;
import static tech.bison.trainee.CloudCondensePocApplication.WEBDAV_PASSWORD;
import static tech.bison.trainee.CloudCondensePocApplication.WEBDAV_URL;
import static tech.bison.trainee.CloudCondensePocApplication.WEBDAV_USERNAME;

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

@Service
public class CloudCondensePocService {
  private final ExecutorService archiveExecutor;

  public CloudCondensePocService(ExecutorService archiveExecutor) {
    this.archiveExecutor = archiveExecutor;
  }

  public void archiveDataByRequest(int archiveDayAge) {
    archiveExecutor.submit(() -> {
      try {
        archiveDataOlderThanDays(archiveDayAge);
        System.out.println(String.format("Data older than %s days archived.", archiveDayAge));
      } catch (IOException e) {
        System.out.println("Archiving failed.");
        e.printStackTrace();
      }
    });
  }

  private void archiveDataOlderThanDays(int days) throws IOException {
    Sardine sardine = SardineFactory.begin(WEBDAV_USERNAME, WEBDAV_PASSWORD);
    List<DavResource> resources = sardine.list(WEBDAV_URL);

    File destDir = new File(TMP_ARCHIVE_WORK_DIR);
    for (DavResource resource : resources) {
      if (resource.getModified()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
          .isBefore(LocalDateTime.now().minusDays(days))) {
        String resourceUrl = WEBDAV_URL + "/" + resource.getName();
        try (InputStream is = sardine.get(resourceUrl)) {
          File targetFile = new File(destDir, resource.getName());
          FileUtils.copyInputStreamToFile(is, targetFile);
        }
      }
    }
  }
}
