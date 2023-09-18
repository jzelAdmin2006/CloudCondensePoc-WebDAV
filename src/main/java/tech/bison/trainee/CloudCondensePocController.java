package tech.bison.trainee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CloudCondensePocController {
  private final CloudCondensePocService cloudCondensePocService;

  public CloudCondensePocController(CloudCondensePocService cloudCondensePocService) {
    this.cloudCondensePocService = cloudCondensePocService;
  }

  @PostMapping("/archive")
  public ResponseEntity<String> backupRepos(@RequestBody String archiveAgeStr) {
    int archiveAge = Integer.parseInt(archiveAgeStr);
    cloudCondensePocService.addArchiveRequestAndProcess(archiveAge);
    return ResponseEntity.status(202).body("Archive creation queued");
  }
}
