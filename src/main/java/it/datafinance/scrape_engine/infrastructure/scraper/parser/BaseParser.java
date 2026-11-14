package it.datafinance.scrape_engine.infrastructure.scraper.parser;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import java.net.URL;

public interface BaseParser {

  ScrapeResult execute(URL siteUrl);
}
