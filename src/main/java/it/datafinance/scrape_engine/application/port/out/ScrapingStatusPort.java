package it.datafinance.scrape_engine.application.port.out;

import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ScrapingStatusPort {
  List<ScrapingStatus> findAll();

  Optional<ScrapingStatus> findById(String id);

  List<ScrapingStatus> findByStartedAt(ZonedDateTime startedAt);

  List<ScrapingStatus> findByEndedAt(ZonedDateTime endedAt);

  List<ScrapingStatus> findByStartedAtBetween(ZonedDateTime start, ZonedDateTime end);
}
