package it.datafinance.scrape_engine.adapter.out.persistence.scraping_status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.datafinance.scrape_engine.adapter.out.persistence.scraping_status.model.ScrapingStatusCollection;
import it.datafinance.scrape_engine.domain.model.ScrapingStatus;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScrapeEngineApplicationTests {

  private static final String ZONE = "Europe/Rome";
  private static final Instant T0 = Instant.parse("2026-11-10T09:00:00Z");
  private static final Instant T1 = T0.plusSeconds(300);

  @Mock private ScrapingStatusRepository repository;
  private ScrapingStatusPortAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ScrapingStatusPortAdapter(repository);
  }

  private static ScrapingStatusCollection collection(
      String id,
      String status,
      int savedDocuments,
      Instant startedAt,
      Instant endedAt,
      String zone) {
    return new ScrapingStatusCollection(
        id, new ObjectId().toHexString(), status, savedDocuments, startedAt, zone, endedAt);
  }

  private static ScrapingStatusCollection completed(String id) {
    return collection(id, "COMPLETED", 120, T0, T1, ZONE);
  }

  @Test
  void should_mapAllFields_when_findByIdFindsDocument() {
    when(repository.findById("1")).thenReturn(Optional.of(completed("1")));

    ScrapingStatus result = adapter.findById("1").orElseThrow();

    assertSoftly(softly -> {
      softly.assertThat(result.id()).isEqualTo("1");
      softly.assertThat(result.status()).isEqualTo("COMPLETED");
      softly.assertThat(result.savedDocuments()).isEqualTo(120);
      softly.assertThat(result.startedAt().toInstant()).isEqualTo(T0);
      assert result.endedAt() != null;
      softly.assertThat(result.endedAt().toInstant()).isEqualTo(T1);
    });
  }

  @Test
  void should_returnEmpty_when_findByIdFindsNothing() {
    when(repository.findById("nothing")).thenReturn(Optional.empty());

    assertThat(adapter.findById("nothing")).isEmpty();
  }

  @Test
  void should_queryByInstant_when_findByStartedAt() {
    ZonedDateTime startedAt = T0.atZone(ZoneId.of(ZONE));
    when(repository.findByStartedAt(T0)).thenReturn(List.of(completed("1")));

    List<ScrapingStatus> result = adapter.findByStartedAt(startedAt);

    assertThat(result).extracting(ScrapingStatus::id).containsExactly("1");
    verify(repository).findByStartedAt(T0);
  }

  @Test
  void should_queryByInstant_when_findByEndedAt() {
    ZonedDateTime endedAt = T1.atZone(ZoneId.of("UTC"));
    when(repository.findByEndedAt(T1)).thenReturn(List.of(completed("1")));

    List<ScrapingStatus> result = adapter.findByEndedAt(endedAt);

    assertThat(result).extracting(ScrapingStatus::id).containsExactly("1");
    verify(repository).findByEndedAt(T1);
  }

  @Test
  void should_convertBothBoundsToInstants_when_findByStartedAtBetween() {
    ZonedDateTime start = T0.atZone(ZoneId.of("UTC"));
    ZonedDateTime end = T1.atZone(ZoneId.of("UTC"));
    when(repository.findByStartedAtBetween(T0, T1))
        .thenReturn(List.of(completed("1"), completed("2")));

    List<ScrapingStatus> result = adapter.findByStartedAtBetween(start, end);
    assertThat(result).hasSize(2);
    verify(repository).findByStartedAtBetween(T0, T1);
  }
}
