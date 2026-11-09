package it.datafinance.scrape_engine.adapter.out.persistence.scraping_status.model;

import com.mongodb.lang.NonNull;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scrape_status")
public record ScrapingStatusCollection(
    @Id String id,
    @NonNull String sourceId,
    @NonNull String status,
    int savedDocuments,
    @NonNull Instant startedAt,
    String dateZone,
    Instant endedAt) {}
