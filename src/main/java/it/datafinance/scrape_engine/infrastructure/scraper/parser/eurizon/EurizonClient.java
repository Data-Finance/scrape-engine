package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedFile;
import java.io.IOException;
import java.net.URL;

interface EurizonClient {

  /** Returns the HTML of the documentation page. */
  String fetchPage(URL pageUrl) throws IOException;

  /** Downloads a PDF, named as the site names it in the Content-Disposition header. */
  ScrapedFile download(URL fileUrl) throws IOException;
}
