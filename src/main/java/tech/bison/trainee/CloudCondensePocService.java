package tech.bison.trainee;

import static tech.bison.trainee.CloudCondensePocApplication.WEBDAV_PASSWORD;
import static tech.bison.trainee.CloudCondensePocApplication.WEBDAV_URL;
import static tech.bison.trainee.CloudCondensePocApplication.WEBDAV_USERNAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

@Service
public class CloudCondensePocService {

  private final ExecutorService archiveExecutor;
  private final Queue<Integer> archiveRequests = new LinkedList<>();

  public CloudCondensePocService(ExecutorService archiveExecutor) {
    this.archiveExecutor = archiveExecutor;
  }


  public void addArchiveRequestAndProcess(Integer archiveDayAge) {
    synchronized (archiveRequests) {
      archiveRequests.add(archiveDayAge);
      processArchiveRequests();
    }
  }

  public void processArchiveRequests() {
    Integer archiveDataDayAge;

    synchronized (archiveRequests) {
      archiveDataDayAge = archiveRequests.poll();
    }

    if (archiveDataDayAge == null)
      return;

    archiveExecutor.submit(() -> {
      try {
        archiveDataOlderThanDays(archiveDataDayAge);
        System.out.println(String.format("Data older than %s days archived.", archiveDataDayAge));
      } catch (IOException e) {
        System.out.println("Archiving failed.");
        e.printStackTrace();
      }
    });
  }

  private void archiveDataOlderThanDays(int days) throws IOException {
    Sardine sardine = SardineFactory.begin(WEBDAV_USERNAME, WEBDAV_PASSWORD);
    List<DavResource> resources = sardine.list(WEBDAV_URL);

    File destDir = new File("C:/tmp");
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
