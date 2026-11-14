package it.datafinance.scrape_engine.infrastructure.scraper;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeRequest;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.parser.BaseParser;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

@Service
class ScraperService implements BaseScraperService {

  private final BeanFactory factory;

  ScraperService(BeanFactory factory) {
    this.factory = factory;
  }

  @Override
  public ScrapeResult execute(ScrapeRequest request) {
    var parser = factory.getBean(request.corporation());
    if (parser instanceof BaseParser) {
      return ((BaseParser) parser).execute(request.siteUrl());
    }
    return new ScrapeResult.Failed(
        "Impossible to find any parser associated to this corporation", "PARSER.NOT_FOUND");
  }
}
