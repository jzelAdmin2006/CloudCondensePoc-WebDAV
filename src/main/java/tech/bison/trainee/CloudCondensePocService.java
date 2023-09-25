package tech.bison.trainee;

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import tech.bison.trainee.config.ArchiveConfig;
import tech.bison.trainee.config.WebDavConfig;
import tech.bison.trainee.service.ArchivingService;
import tech.bison.trainee.service.ResourceService;
import tech.bison.trainee.storage.CloudStorage;
import tech.bison.trainee.storage.WebDavStorage;

@Service
public class CloudCondensePocService {
  private static final Logger LOGGER = Logger.getLogger(CloudCondensePocService.class.getName());

  private final ExecutorService archiveExecutor;
  private final ArchivingService archivingService;
  private final ResourceService resourceService;
  private final CloudStorage storage;

  public CloudCondensePocService(ExecutorService archiveExecutor, WebDavConfig webDavConfig,
      ArchiveConfig archiveConfig) {
    this.archiveExecutor = archiveExecutor;
    this.storage = new WebDavStorage(webDavConfig);
    this.resourceService = new ResourceService(storage);
    this.archivingService = new ArchivingService(archiveConfig);
  }

  public void archiveDataByRequest(int archiveDayAge) {
    archiveExecutor.submit(() -> {
      try {
        archivingService
            .archiveDataOlderThanDays(archiveDayAge, resourceService, ((WebDavStorage) storage).getConfig().getUrl());
        LOGGER.log(Level.INFO, "Data older than %s days archived.".formatted(archiveDayAge));
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Archiving failed.", e);
      }
    });
  }
}
