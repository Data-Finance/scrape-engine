package it.datafinance.scrape_engine.infrastructure.scraper.model;

public record ScrapedProduct(String productName, String isin, ScrapedFile file) {}
