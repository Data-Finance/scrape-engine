package it.datafinance.scrape_engine.infrastructure.scraper.model;

import java.util.List;

public sealed interface ScrapeResult {

  record Success(List<ScrapedProduct> products) implements ScrapeResult {}

  record Failed(String message, String code) implements ScrapeResult {}
}
