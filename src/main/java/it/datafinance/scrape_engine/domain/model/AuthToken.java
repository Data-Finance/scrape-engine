package it.datafinance.scrape_engine.domain.model;

import java.time.Instant;

/** A signed access token issued to an authenticated user. */
public record AuthToken(String value, Instant expiresAt) {

  public AuthToken {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
    if (expiresAt == null) {
      throw new IllegalArgumentException("expiresAt must not be null");
    }
  }
}
