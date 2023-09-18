package tech.bison.trainee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CloudCondensePocController {
  @PostMapping("/archive")
  public ResponseEntity<String> backupRepos(@RequestBody String archiveAgeStr) {
    int archiveAge = Integer.parseInt(archiveAgeStr);
    try {
      CloudCondensePocService.getArchiveRequests().add(archiveAge);
      return ResponseEntity.status(202).body("Archive creation queued");
    } catch (Exception e) {
      System.err.println("Error creating queue");
      e.printStackTrace();
      return ResponseEntity.status(500).body("Error creating queue:\n" + e.getStackTrace());
    }
  }
}
