package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedFile;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedProduct;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class EurizonParserTest {

  private static final byte[] PDF = "%PDF-1.5".getBytes();

  private final URL siteUrl = url("https://www.eurizoncapital.com/it-IT/offerta/documentazione");
  private final URL italianKidUrl = downloadUrl("HHGGGE");
  private final URL luxembourgKidUrl = downloadUrl("HHGGEB");

  private final EurizonClient client = mock(EurizonClient.class);
  private final EurizonParser parser = parserLimitedTo(0);

  @Test
  void should_returnKidsWithIsinFromFileName_when_pageListsKids() throws IOException {
    ScrapedFile italianKid = new ScrapedFile("55_11788-1_IT0005506610.pdf", PDF);
    ScrapedFile luxembourgKid = new ScrapedFile("55_1029-2_LU0090981274.pdf", PDF);
    when(client.fetchPage(siteUrl)).thenReturn(EurizonFixtures.PAGE);
    when(client.download(italianKidUrl)).thenReturn(italianKid);
    when(client.download(luxembourgKidUrl)).thenReturn(luxembourgKid);

    ScrapeResult result = parser.execute(siteUrl);

    assertSoftly(softly -> {
      softly
          .assertThat(result)
          .isEqualTo(
              new ScrapeResult.Success(
                  List.of(
                      new ScrapedProduct("Eurizon Investo Smart 10", "IT0005506610", italianKid),
                      new ScrapedProduct(
                          "Eurizon Fund - Equity Emerging Markets Smart Volatility R",
                          "LU0090981274",
                          luxembourgKid))));
      // The prospectus listed in the page is not a KID and must not be downloaded
      softly
          .assertThatCode(() -> verify(client, times(2)).download(any()))
          .doesNotThrowAnyException();
    });
  }

  @Test
  void should_downloadOnlyTheFirstKids_when_limitIsSet() throws IOException {
    ScrapedFile italianKid = new ScrapedFile("55_11788-1_IT0005506610.pdf", PDF);
    when(client.fetchPage(siteUrl)).thenReturn(EurizonFixtures.PAGE);
    when(client.download(italianKidUrl)).thenReturn(italianKid);

    ScrapeResult result = parserLimitedTo(1).execute(siteUrl);

    assertSoftly(softly -> {
      softly
          .assertThat(result)
          .isEqualTo(
              new ScrapeResult.Success(
                  List.of(
                      new ScrapedProduct("Eurizon Investo Smart 10", "IT0005506610", italianKid))));
      softly
          .assertThatCode(() -> verify(client, never()).download(luxembourgKidUrl))
          .doesNotThrowAnyException();
    });
  }

  @Test
  void should_skipDocument_when_itsDownloadFails() throws IOException {
    ScrapedFile luxembourgKid = new ScrapedFile("55_1029-2_LU0090981274.pdf", PDF);
    when(client.fetchPage(siteUrl)).thenReturn(EurizonFixtures.PAGE);
    when(client.download(italianKidUrl)).thenThrow(new IOException("HTTP 500"));
    when(client.download(luxembourgKidUrl)).thenReturn(luxembourgKid);

    ScrapeResult result = parser.execute(siteUrl);

    assertSoftly(softly ->
        softly
            .assertThat(result)
            .isEqualTo(
                new ScrapeResult.Success(
                    List.of(
                        new ScrapedProduct(
                            "Eurizon Fund - Equity Emerging Markets Smart Volatility R",
                            "LU0090981274",
                            luxembourgKid)))));
  }

  @Test
  void should_skipDocument_when_fileNameHasNoIsin() throws IOException {
    ScrapedFile luxembourgKid = new ScrapedFile("55_1029-2_LU0090981274.pdf", PDF);
    when(client.fetchPage(siteUrl)).thenReturn(EurizonFixtures.PAGE);
    when(client.download(italianKidUrl)).thenReturn(new ScrapedFile("document.pdf", PDF));
    when(client.download(luxembourgKidUrl)).thenReturn(luxembourgKid);

    ScrapeResult result = parser.execute(siteUrl);

    assertSoftly(softly ->
        softly
            .assertThat(result)
            .isInstanceOfSatisfying(
                ScrapeResult.Success.class,
                success ->
                    softly
                        .assertThat(success.products())
                        .extracting(ScrapedProduct::isin)
                        .containsExactly("LU0090981274")));
  }

  @Test
  void should_failAndStopDownloading_when_accessIsDenied() throws IOException {
    when(client.fetchPage(siteUrl)).thenReturn(EurizonFixtures.PAGE);
    when(client.download(italianKidUrl))
        .thenThrow(new EurizonAccessDeniedException(italianKidUrl.toString()));

    ScrapeResult result = parser.execute(siteUrl);

    assertSoftly(softly -> {
      softly
          .assertThat(result)
          .isInstanceOfSatisfying(
              ScrapeResult.Failed.class,
              failed -> softly.assertThat(failed.code()).isEqualTo("EURIZON.ACCESS_DENIED"));
      softly
          .assertThatCode(() -> verify(client, never()).download(luxembourgKidUrl))
          .doesNotThrowAnyException();
    });
  }

  @Test
  void should_fail_when_pageAccessIsDenied() throws IOException {
    when(client.fetchPage(siteUrl)).thenThrow(new EurizonAccessDeniedException(siteUrl.toString()));

    assertSoftly(
        softly -> assertFailedWith(softly, parser.execute(siteUrl), "EURIZON.ACCESS_DENIED"));
  }

  @Test
  void should_fail_when_pageCannotBeFetched() throws IOException {
    when(client.fetchPage(siteUrl)).thenThrow(new IOException("timeout"));

    assertSoftly(
        softly -> assertFailedWith(softly, parser.execute(siteUrl), "EURIZON.PAGE_UNREACHABLE"));
  }

  @Test
  void should_fail_when_pageListsNoKid() throws IOException {
    when(client.fetchPage(siteUrl)).thenReturn("<html><body></body></html>");

    assertSoftly(
        softly -> assertFailedWith(softly, parser.execute(siteUrl), "EURIZON.KID_NOT_FOUND"));
  }

  @Test
  void should_fail_when_noKidCanBeDownloaded() throws IOException {
    when(client.fetchPage(siteUrl)).thenReturn(EurizonFixtures.PAGE);
    when(client.download(any())).thenThrow(new IOException("HTTP 500"));

    assertSoftly(
        softly -> assertFailedWith(softly, parser.execute(siteUrl), "EURIZON.DOWNLOAD_FAILED"));
  }

  private EurizonParser parserLimitedTo(int maxKids) {
    return new EurizonParser(
        client, new EurizonPageExtractor(JsonMapper.builder().build()), Duration.ZERO, maxKids);
  }

  private static void assertFailedWith(SoftAssertions softly, ScrapeResult result, String code) {
    softly
        .assertThat(result)
        .isInstanceOfSatisfying(
            ScrapeResult.Failed.class, failed -> softly.assertThat(failed.code()).isEqualTo(code));
  }

  private static URL downloadUrl(String documentId) {
    return url(
        "https://www.eurizoncapital.com/api/Sitecore/ProductFinderApi/DownloadItalianCountryDocument?documentId="
            + documentId);
  }

  private static URL url(String value) {
    try {
      return URI.create(value).toURL();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
