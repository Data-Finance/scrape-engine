package it.datafinance.scrape_engine.infrastructure.scraper;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeRequest;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;

public interface BaseScraperService {

  ScrapeResult execute(ScrapeRequest request);
}
