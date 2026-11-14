package it.datafinance.scrape_engine.infrastructure.scraper;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeRequest;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.parser.BaseParser;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

@Service
class ScraperService implements BaseScraperService {

  private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

  private final BeanFactory factory;

  ScraperService(BeanFactory factory) {
    this.factory = factory;
  }

  @Override
  public ScrapeResult execute(ScrapeRequest request) {
    log.info("Scrape requested for {} on {}", request.corporation(), request.siteUrl());
    // getBean throws for an unknown name, so the name is checked first
    if (factory.containsBean(request.corporation())
        && factory.getBean(request.corporation()) instanceof BaseParser parser) {
      log.debug("Scrape for {} handled by {}", request.corporation(), parser.getClass().getName());
      long startedAt = System.nanoTime();
      ScrapeResult result = parser.execute(request.siteUrl());
      logOutcome(request, result, Duration.ofNanos(System.nanoTime() - startedAt));
      return result;
    }
    log.warn("Scrape for {} failed: no parser is registered for it", request.corporation());
    return new ScrapeResult.Failed(
        "Impossible to find any parser associated to this corporation", "PARSER.NOT_FOUND");
  }

  private void logOutcome(ScrapeRequest request, ScrapeResult result, Duration elapsed) {
    Duration took = elapsed.truncatedTo(ChronoUnit.MILLIS);
    switch (result) {
      case ScrapeResult.Success success ->
          log.info(
              "Scrape for {} succeeded in {}: {} products",
              request.corporation(),
              took,
              success.products().size());
      case ScrapeResult.Failed failed ->
          log.warn(
              "Scrape for {} failed in {}: [{}] {}",
              request.corporation(),
              took,
              failed.code(),
              failed.message());
    }
  }
}
