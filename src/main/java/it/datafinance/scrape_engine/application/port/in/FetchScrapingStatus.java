package it.datafinance.scrape_engine.application.port.in;

import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.util.List;

public interface FetchScrapingStatus {
  List<ScrapingStatus> getAll();
}
