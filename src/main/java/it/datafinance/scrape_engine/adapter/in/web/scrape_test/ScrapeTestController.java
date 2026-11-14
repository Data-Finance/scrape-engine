package it.datafinance.scrape_engine.adapter.in.web.scrape_test;

import it.datafinance.scrape_engine.adapter.in.web.scrape_test.dto.ScrapeTestResponseDto;
import it.datafinance.scrape_engine.adapter.in.web.scrape_test.dto.ScrapedProductDto;
import it.datafinance.scrape_engine.infrastructure.scraper.BaseScraperService;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeRequest;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedProduct;
import java.net.URL;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Temporary endpoint to try a scrape by hand. It needs no authentication, so it only exists when
 * {@code scraper.test-api.enabled=true}. It calls the scraper directly instead of going through a
 * use case: replace it with a proper inbound port once the scrape flow is designed.
 */
@RestController
@RequestMapping("/scrape-test")
@ConditionalOnProperty(name = "scraper.test-api.enabled", havingValue = "true")
class ScrapeTestController {

  private final BaseScraperService scraperService;

  ScrapeTestController(BaseScraperService scraperService) {
    this.scraperService = scraperService;
  }

  /** Runs the scrape synchronously: the response arrives only when every file is downloaded. */
  @GetMapping("/{corporation}")
  ResponseEntity<ScrapeTestResponseDto> scrape(
      @PathVariable String corporation, @RequestParam URL url) {
    return switch (scraperService.execute(new ScrapeRequest(corporation, url))) {
      case ScrapeResult.Success success -> {
        List<ScrapedProductDto> products = success.products().stream().map(this::toDto).toList();
        yield ResponseEntity.ok(
            new ScrapeTestResponseDto(true, null, null, products.size(), products));
      }
      case ScrapeResult.Failed failed ->
          ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body(
                  new ScrapeTestResponseDto(false, failed.code(), failed.message(), 0, List.of()));
    };
  }

  private ScrapedProductDto toDto(ScrapedProduct product) {
    return new ScrapedProductDto(
        product.productName(),
        product.isin(),
        product.file().fileName(),
        product.file().data().length);
  }
}
