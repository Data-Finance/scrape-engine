package it.datafinance.scrape_engine.adapter.in.web.scrape_test.dto;

/** A scraped product without the file content, which would make the response huge. */
public record ScrapedProductDto(String productName, String isin, String fileName, int sizeBytes) {}
