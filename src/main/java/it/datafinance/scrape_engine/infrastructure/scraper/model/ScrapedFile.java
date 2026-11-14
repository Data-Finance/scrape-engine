package it.datafinance.scrape_engine.infrastructure.scraper.model;

public record ScrapedFile(String fileName, byte[] data) {}
