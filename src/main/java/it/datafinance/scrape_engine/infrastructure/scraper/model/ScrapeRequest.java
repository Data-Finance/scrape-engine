package it.datafinance.scrape_engine.infrastructure.scraper.model;

import java.net.URL;

public record ScrapeRequest(String corporation, URL siteUrl) {}
