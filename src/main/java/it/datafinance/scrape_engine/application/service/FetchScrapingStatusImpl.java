package it.datafinance.scrape_engine.application.service;

import it.datafinance.scrape_engine.application.ApplicationService;
import it.datafinance.scrape_engine.application.port.in.FetchScrapingStatus;
import it.datafinance.scrape_engine.application.port.out.ScrapingStatusPort;
import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationService
class FetchScrapingStatusImpl implements FetchScrapingStatus {

  private final ScrapingStatusPort scrapingStatusPort;

  public FetchScrapingStatusImpl(ScrapingStatusPort scrapingStatusPort) {
    this.scrapingStatusPort = scrapingStatusPort;
  }

  public List<ScrapingStatus> getAll() {
    return scrapingStatusPort.findAll();
  }

  public Optional<ScrapingStatus> getById(String id) {
    return scrapingStatusPort.findById(id);
  }

  public List<ScrapingStatus> getByStartedAt(ZonedDateTime startedAt) {
    return scrapingStatusPort.findByStartedAt(startedAt);
  }

  public List<ScrapingStatus> getByEndedAt(ZonedDateTime endedAt) {
    return scrapingStatusPort.findByEndedAt(endedAt);
  }

  public List<ScrapingStatus> getByStartedAtBetween(ZonedDateTime start, ZonedDateTime end) {
    return scrapingStatusPort.findByStartedAtBetween(start, end);
  }
}
