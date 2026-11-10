package it.datafinance.scrape_engine.adapter.in.web.scraping_status.model;

import java.time.ZonedDateTime;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ScrapingStatusResource(
    String id,
    String status,
    int savedDocuments,
    ZonedDateTime startedAt,
    @Nullable ZonedDateTime endedAt) {}
