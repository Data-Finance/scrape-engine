package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import java.io.IOException;

/** The site refused the request (HTTP 403), typically because of its rate limiting. */
class EurizonAccessDeniedException extends IOException {

  EurizonAccessDeniedException(String url) {
    super("Access denied by Eurizon for " + url);
  }
}
