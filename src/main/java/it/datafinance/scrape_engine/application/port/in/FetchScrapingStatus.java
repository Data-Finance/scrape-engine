package it.datafinance.scrape_engine.application.port.in;

import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface FetchScrapingStatus {
  List<ScrapingStatus> getAll();

  Optional<ScrapingStatus> getById(String id);

  List<ScrapingStatus> getByStartedAt(ZonedDateTime startedAt);

  List<ScrapingStatus> getByEndedAt(ZonedDateTime endedAt);

  List<ScrapingStatus> getByStartedAtBetween(ZonedDateTime start, ZonedDateTime end);
}
