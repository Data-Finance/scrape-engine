package it.datafinance.scrape_engine.infrastructure.scraper;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeRequest;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.parser.BaseParser;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class ScraperServiceTest {

  private final URL siteUrl = url("https://example.com/documents");
  private final ScrapeResult parserResult = new ScrapeResult.Success(List.of());
  private final StaticListableBeanFactory factory = new StaticListableBeanFactory();
  private final ScraperService service = new ScraperService(factory);

  @Test
  void should_delegateToParserNamedAfterCorporation_when_parserExists() {
    factory.addBean("acme", (BaseParser) url -> url.equals(siteUrl) ? parserResult : null);

    ScrapeResult result = service.execute(new ScrapeRequest("acme", siteUrl));

    assertSoftly(softly -> softly.assertThat(result).isSameAs(parserResult));
  }

  @Test
  void should_failWithParserNotFound_when_noBeanHasThatName() {
    ScrapeResult result = service.execute(new ScrapeRequest("unknown", siteUrl));

    assertSoftly(softly -> assertParserNotFound(softly, result));
  }

  @Test
  void should_failWithParserNotFound_when_beanIsNotAParser() {
    factory.addBean("acme", "not a parser");

    ScrapeResult result = service.execute(new ScrapeRequest("acme", siteUrl));

    assertSoftly(softly -> assertParserNotFound(softly, result));
  }

  private static void assertParserNotFound(SoftAssertions softly, ScrapeResult result) {
    softly
        .assertThat(result)
        .isInstanceOfSatisfying(
            ScrapeResult.Failed.class,
            failed -> softly.assertThat(failed.code()).isEqualTo("PARSER.NOT_FOUND"));
  }

  private static URL url(String value) {
    try {
      return URI.create(value).toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
