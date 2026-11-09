package it.datafinance.scrape_engine.domain.model;

import java.time.ZonedDateTime;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ScrapingStatus(
    String id,
    String status,
    int savedDocuments,
    ZonedDateTime startedAt,
    @Nullable ZonedDateTime endedAt) {}
