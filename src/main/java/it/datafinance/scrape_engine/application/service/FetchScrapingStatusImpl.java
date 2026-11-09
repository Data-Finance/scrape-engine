package it.datafinance.scrape_engine.application.service;

import it.datafinance.scrape_engine.application.ApplicationService;
import it.datafinance.scrape_engine.application.port.in.FetchScrapingStatus;
import it.datafinance.scrape_engine.application.port.out.ScrapingStatusPort;
import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.util.List;

@ApplicationService
class FetchScrapingStatusImpl implements FetchScrapingStatus {

  private final ScrapingStatusPort scrapingStatusPort;

  public FetchScrapingStatusImpl(ScrapingStatusPort scrapingStatusPort) {
    this.scrapingStatusPort = scrapingStatusPort;
  }

  public List<ScrapingStatus> getAll() {
    return scrapingStatusPort.findAll();
  }
}
