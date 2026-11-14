package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedFile;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class JsoupEurizonClient implements EurizonClient {

  private static final Logger log = LoggerFactory.getLogger(JsoupEurizonClient.class);

  private static final Duration TIMEOUT = Duration.ofSeconds(60);
  private static final String PDF_CONTENT_TYPE = "application/pdf";

  // The session keeps cookies: the page answers with a redirect to itself until they are set.
  private final Connection session;

  JsoupEurizonClient(@Value("${scraper.eurizon.user-agent}") String userAgent) {
    this.session =
        Jsoup.newSession()
            .userAgent(userAgent)
            .timeout((int) TIMEOUT.toMillis())
            // The page embeds the whole catalogue (>2 MB), above jsoup's default body limit.
            .maxBodySize(0)
            .ignoreContentType(true)
            .ignoreHttpErrors(true);
  }

  @Override
  public String fetchPage(URL pageUrl) throws IOException {
    return get(pageUrl).body();
  }

  @Override
  public ScrapedFile download(URL fileUrl) throws IOException {
    Connection.Response response = get(fileUrl);
    String contentType = response.contentType();
    if (contentType == null || !contentType.startsWith(PDF_CONTENT_TYPE)) {
      throw new IOException("Expected a PDF from " + fileUrl + " but got " + contentType);
    }
    String disposition = response.header("Content-Disposition");
    String fileName =
        disposition == null ? null : ContentDisposition.parse(disposition).getFilename();
    if (fileName == null || fileName.isBlank()) {
      throw new IOException("No file name in the response from " + fileUrl);
    }
    return new ScrapedFile(fileName, response.bodyAsBytes());
  }

  private Connection.Response get(URL url) throws IOException {
    long startedAt = System.nanoTime();
    Connection.Response response;
    try {
      response = session.newRequest(url).execute();
    } catch (IOException e) {
      log.debug("GET {} failed: {}", url, e.toString());
      throw e;
    }
    int status = response.statusCode();
    log.debug(
        "GET {} -> HTTP {} ({}, {} ms)",
        url,
        status,
        response.contentType(),
        Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
    if (status == HttpStatus.FORBIDDEN.value()) {
      throw new EurizonAccessDeniedException(url.toString());
    }
    if (status < 200 || status >= 300) {
      throw new IOException("HTTP " + status + " from " + url);
    }
    return response;
  }
}
