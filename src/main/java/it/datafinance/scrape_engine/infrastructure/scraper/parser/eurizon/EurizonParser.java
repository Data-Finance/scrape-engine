package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedFile;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedProduct;
import it.datafinance.scrape_engine.infrastructure.scraper.parser.BaseParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Extracts the KID PDFs of the Italian and Luxembourg funds from the Eurizon documentation page.
 * The bean name is the corporation that {@code ScraperService} resolves parsers by.
 */
@Component(EurizonParser.CORPORATION)
class EurizonParser implements BaseParser {

  static final String CORPORATION = "eurizon";

  private static final Logger log = LoggerFactory.getLogger(EurizonParser.class);

  private static final String KID_DOC_TYPE = "KID";
  private static final int PROGRESS_LOG_INTERVAL = 25;
  private static final String DOWNLOAD_PATH =
      "/api/Sitecore/ProductFinderApi/DownloadItalianCountryDocument?documentId=";
  // The page data has no ISIN: the site puts it in the file name, e.g. 55_11788-1_IT0005506610.pdf
  private static final Pattern ISIN =
      Pattern.compile("(?<![A-Z0-9])[A-Z]{2}[A-Z0-9]{9}[0-9](?![A-Z0-9])");

  private final EurizonClient client;
  private final EurizonPageExtractor extractor;
  private final Duration downloadDelay;
  private final int maxKids;

  EurizonParser(
      EurizonClient client,
      EurizonPageExtractor extractor,
      @Value("${scraper.eurizon.download-delay}") Duration downloadDelay,
      @Value("${scraper.eurizon.max-kids}") int maxKids) {
    this.client = client;
    this.extractor = extractor;
    this.downloadDelay = downloadDelay;
    this.maxKids = maxKids;
  }

  @Override
  public ScrapeResult execute(URL siteUrl) {
    long startedAt = System.nanoTime();
    log.info("Eurizon scrape started on {}", siteUrl);
    List<EurizonDocument> kids;
    try {
      String html = client.fetchPage(siteUrl);
      log.info("Eurizon page fetched ({} characters)", html.length());
      List<EurizonDocument> documents = extractor.extract(html);
      kids = distinctKids(documents);
      log.info("Eurizon page lists {} documents, {} of them KIDs", documents.size(), kids.size());
    } catch (EurizonAccessDeniedException e) {
      return accessDenied(e);
    } catch (IOException e) {
      log.warn("Eurizon page {} could not be fetched", siteUrl, e);
      return new ScrapeResult.Failed(
          "Impossible to fetch the Eurizon documentation page", "EURIZON.PAGE_UNREACHABLE");
    }
    if (maxKids > 0 && kids.size() > maxKids) {
      log.info("Eurizon scrape limited to the first {} of {} KIDs", maxKids, kids.size());
      kids = kids.subList(0, maxKids);
    }
    if (kids.isEmpty()) {
      log.warn("Eurizon scrape failed: no KID document found in {}", siteUrl);
      return new ScrapeResult.Failed(
          "No KID document found in the Eurizon documentation page", "EURIZON.KID_NOT_FOUND");
    }

    List<ScrapedProduct> products = new ArrayList<>();
    try {
      for (int i = 0; i < kids.size(); i++) {
        if (i > 0) {
          Thread.sleep(downloadDelay);
        }
        EurizonDocument kid = kids.get(i);
        log.debug("Eurizon KID {}/{}: downloading {}", i + 1, kids.size(), kid.name());
        download(siteUrl, kid).ifPresent(products::add);
        if ((i + 1) % PROGRESS_LOG_INTERVAL == 0) {
          log.info(
              "Eurizon progress: {}/{} KIDs processed, {} downloaded, {} skipped",
              i + 1,
              kids.size(),
              products.size(),
              i + 1 - products.size());
        }
      }
    } catch (EurizonAccessDeniedException e) {
      log.warn(
          "Eurizon access denied after {} of {} KIDs were downloaded, which are discarded",
          products.size(),
          kids.size());
      return accessDenied(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn(
          "Eurizon scrape interrupted after {} of {} KIDs were downloaded",
          products.size(),
          kids.size());
      return new ScrapeResult.Failed("Eurizon scrape interrupted", "EURIZON.INTERRUPTED");
    }
    log.info(
        "Eurizon scrape finished in {}: {} of {} KIDs downloaded, {} skipped",
        Duration.ofNanos(System.nanoTime() - startedAt).truncatedTo(ChronoUnit.MILLIS),
        products.size(),
        kids.size(),
        kids.size() - products.size());
    if (products.isEmpty()) {
      log.warn("Eurizon scrape failed: none of the {} KIDs could be downloaded", kids.size());
      return new ScrapeResult.Failed(
          "No Eurizon KID document could be downloaded", "EURIZON.DOWNLOAD_FAILED");
    }
    return new ScrapeResult.Success(products);
  }

  private List<EurizonDocument> distinctKids(List<EurizonDocument> documents) {
    Set<String> seen = new HashSet<>();
    return documents.stream()
        .filter(document -> KID_DOC_TYPE.equals(document.docType()))
        .filter(document -> seen.add(document.encryptedFileId()))
        .toList();
  }

  /** A document that cannot be downloaded is skipped, unless the site is denying access. */
  private Optional<ScrapedProduct> download(URL siteUrl, EurizonDocument kid)
      throws EurizonAccessDeniedException {
    try {
      ScrapedFile file = client.download(downloadUrl(siteUrl, kid));
      Matcher isin = ISIN.matcher(file.fileName());
      if (!isin.find()) {
        log.warn("Eurizon KID {} skipped: no ISIN in file name {}", kid.name(), file.fileName());
        return Optional.empty();
      }
      log.debug(
          "Eurizon KID {} downloaded: {} ({} bytes, ISIN {})",
          kid.name(),
          file.fileName(),
          file.data().length,
          isin.group());
      return Optional.of(new ScrapedProduct(kid.name(), isin.group(), file));
    } catch (EurizonAccessDeniedException e) {
      throw e;
    } catch (IOException | URISyntaxException e) {
      log.warn("Eurizon KID {} skipped: download failed ({})", kid.name(), e.toString());
      return Optional.empty();
    }
  }

  private URL downloadUrl(URL siteUrl, EurizonDocument kid) throws IOException, URISyntaxException {
    String documentId = URLEncoder.encode(kid.encryptedFileId(), StandardCharsets.UTF_8);
    return siteUrl.toURI().resolve(DOWNLOAD_PATH + documentId).toURL();
  }

  private ScrapeResult accessDenied(EurizonAccessDeniedException e) {
    log.warn("Eurizon scrape stopped: {}", e.getMessage());
    return new ScrapeResult.Failed(
        "Eurizon denied access, probably because of rate limiting", "EURIZON.ACCESS_DENIED");
  }
}
