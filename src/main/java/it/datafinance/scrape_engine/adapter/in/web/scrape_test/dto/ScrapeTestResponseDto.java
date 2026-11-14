package it.datafinance.scrape_engine.adapter.in.web.scrape_test.dto;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record ScrapeTestResponseDto(
    boolean success,
    @Nullable String errorCode,
    @Nullable String errorMessage,
    int productCount,
    List<ScrapedProductDto> products) {}
