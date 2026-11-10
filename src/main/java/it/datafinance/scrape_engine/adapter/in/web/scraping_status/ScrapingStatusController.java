package it.datafinance.scrape_engine.adapter.in.web.scraping_status;

import it.datafinance.scrape_engine.adapter.in.web.scraping_status.model.ScrapingStatusResource;
import it.datafinance.scrape_engine.application.port.in.FetchScrapingStatus;
import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("scraping-status")
class ScrapingStatusController {
  private final FetchScrapingStatus fetchScrapingStatus;

  public ScrapingStatusController(FetchScrapingStatus fetchScrapingStatus) {
    this.fetchScrapingStatus = fetchScrapingStatus;
  }

  @GetMapping("/retrieve-all")
  public ResponseEntity<List<ScrapingStatusResource>> getAll() {
    return ResponseEntity.ok(
        fetchScrapingStatus.getAll().stream().map(this::domainModelToResource).toList());
  }

  @GetMapping("/scraping-resources/{id}")
  public ResponseEntity<ScrapingStatusResource> getById(@PathVariable String id) {
    return fetchScrapingStatus
        .getById(id)
        .map(this::domainModelToResource)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/scraping-resources?startedAt={startedAt}")
  public ResponseEntity<List<ScrapingStatusResource>> getByStartedAt(
      @PathVariable ZonedDateTime startedAt) {
    return ResponseEntity.ok(
        fetchScrapingStatus.getByStartedAt(startedAt).stream()
            .map(this::domainModelToResource)
            .toList());
  }

  @GetMapping("/scraping-resources?endedAt={endedAt}")
  public ResponseEntity<List<ScrapingStatusResource>> getByEndedAt(
      @PathVariable ZonedDateTime endedAt) {
    return ResponseEntity.ok(
        fetchScrapingStatus.getByEndedAt(endedAt).stream()
            .map(this::domainModelToResource)
            .toList());
  }

  @GetMapping("/scraping-resources?startedAtFrom={start}&startedAtEnd={end}")
  public ResponseEntity<List<ScrapingStatusResource>> getByStartedAtBetween(
      @PathVariable("start") ZonedDateTime start, @PathVariable("end") ZonedDateTime end) {
    return ResponseEntity.ok(
        fetchScrapingStatus.getByStartedAtBetween(start, end).stream()
            .map(this::domainModelToResource)
            .toList());
  }

  private ScrapingStatusResource domainModelToResource(ScrapingStatus scrapingStatus) {
    return new ScrapingStatusResource(
        scrapingStatus.id(),
        scrapingStatus.status(),
        scrapingStatus.savedDocuments(),
        scrapingStatus.startedAt(),
        scrapingStatus.endedAt());
  }
}
