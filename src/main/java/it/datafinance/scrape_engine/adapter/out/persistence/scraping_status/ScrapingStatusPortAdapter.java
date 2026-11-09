package it.datafinance.scrape_engine.adapter.out.persistence.scraping_status;

import it.datafinance.scrape_engine.adapter.out.persistence.scraping_status.model.ScrapingStatusCollection;
import it.datafinance.scrape_engine.application.port.out.ScrapingStatusPort;
import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import it.datafinance.scrape_engine.utils.DateUtils;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
class ScrapingStatusPortAdapter implements ScrapingStatusPort {

  private final ScrapingStatusRepository scrapingStatusRepository;

  ScrapingStatusPortAdapter(ScrapingStatusRepository scrapingStatusRepository) {
    this.scrapingStatusRepository = scrapingStatusRepository;
  }

  @Override
  public List<ScrapingStatus> findAll() {
    return scrapingStatusRepository.findAll().stream()
        .map(this::convertCollectionToDomainModel)
        .toList();
  }

  @Override
  public Optional<ScrapingStatus> findById(String id) {
    return scrapingStatusRepository.findById(id).map(this::convertCollectionToDomainModel);
  }

  @Override
  public List<ScrapingStatus> findByStartedAt(ZonedDateTime startedAt) {
    return scrapingStatusRepository.findByStartedAt(startedAt.toInstant()).stream()
        .map(this::convertCollectionToDomainModel)
        .toList();
  }

  @Override
  public List<ScrapingStatus> findByEndedAt(ZonedDateTime endedAt) {
    return scrapingStatusRepository.findByEndedAt(endedAt.toInstant()).stream()
        .map(this::convertCollectionToDomainModel)
        .toList();
  }

  @Override
  public List<ScrapingStatus> findByStartedAtBetween(ZonedDateTime start, ZonedDateTime end) {
    /*
    return scrapingStatusRepository
        .findByStartedAtBetween(start.toInstant(), end.toInstant())
        .stream()
        .map(this::convertCollectionToDomainModel)
        .toList();
     */
    return List.of();
  }

  private ScrapingStatus convertCollectionToDomainModel(
      @NonNull ScrapingStatusCollection collection) {
    return new ScrapingStatus(
        collection.id(),
        collection.status(),
        collection.savedDocuments(),
        ZonedDateTime.ofInstant(collection.startedAt(), DateUtils.getZoneId(collection.dateZone())),
        ZonedDateTime.ofInstant(collection.endedAt(), DateUtils.getZoneId(collection.dateZone())));
  }
}
