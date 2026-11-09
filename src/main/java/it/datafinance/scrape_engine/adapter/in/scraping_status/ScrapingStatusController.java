package it.datafinance.scrape_engine.adapter.in.scraping_status;

import it.datafinance.scrape_engine.application.port.in.FetchScrapingStatus;
import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

  private ScrapingStatusResource domainModelToResource(ScrapingStatus scrapingStatus) {
    return new ScrapingStatusResource(
        scrapingStatus.id(),
        scrapingStatus.status(),
        scrapingStatus.savedDocuments(),
        scrapingStatus.startedAt(),
        scrapingStatus.endedAt());
  }
}
