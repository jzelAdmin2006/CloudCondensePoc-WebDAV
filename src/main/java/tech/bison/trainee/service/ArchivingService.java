package tech.bison.trainee.service;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static tech.bison.util.sevenzip.SevenZip.SEVEN_ZIP_FILE_ENDING;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import tech.bison.trainee.config.ArchiveConfig;
import tech.bison.trainee.storage.CloudResource;
import tech.bison.util.sevenzip.SevenZip;

public class ArchivingService {
  private final ArchiveConfig archiveConfig;

  public ArchivingService(ArchiveConfig archiveConfig) {
    this.archiveConfig = archiveConfig;
  }

  public void archiveDataOlderThanDays(int days, ResourceService resourceService, String baseUrl) throws IOException {
    List<CloudResource> resources = resourceService.listResources(baseUrl);

    for (CloudResource resource : resources) {
      if (resource.getLastModified().isBefore(LocalDateTime.now().minusDays(days))) {
        archive(resource, resourceService);
      }
    }
  }

  private void archive(CloudResource resource, ResourceService resourceService) throws IOException {
    File tmpWorkDir = new File(archiveConfig.getTmpWorkDir());
    File target = new File(tmpWorkDir, resource.getName());
    try {
      if (resource.isDirectory()) {
        resourceService.copyResourceToFolder(resource, target);
      } else {
        resourceService.copyResourceToFile(resource, target);
      }
      File archive = new File(archiveConfig.getTmpWorkDir(), target.getName() + SEVEN_ZIP_FILE_ENDING);
      SevenZip compressor = new SevenZip();
      compressor.compress(target, archive);
    } finally {
      cleanDirectory(tmpWorkDir);
    }
  }
}
