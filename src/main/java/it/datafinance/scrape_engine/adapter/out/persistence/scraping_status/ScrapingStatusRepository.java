package it.datafinance.scrape_engine.adapter.out.persistence.scraping_status;

import it.datafinance.scrape_engine.adapter.out.persistence.scraping_status.model.ScrapingStatusCollection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScrapingStatusRepository
    extends MongoRepository<ScrapingStatusCollection, String> {
  List<ScrapingStatusCollection> findByStartedAt(Instant startedAt);

  List<ScrapingStatusCollection> findByEndedAt(Instant endedAt);

  // List<ScrapingStatusCollection> findByStartedAtBetween(Instant startedAt, Instant endedAt);
}
