package it.datafinance.scrape_engine.application.port.out;

import org.jspecify.annotations.Nullable;

public interface PasswordVerifier {

  /**
   * Checks a raw password against a stored hash.
   *
   * @param passwordHash the stored hash, or {@code null} when the user does not exist. The
   *     implementation must still spend comparable time so that response timing does not reveal
   *     which usernames exist.
   */
  boolean matches(String rawPassword, @Nullable String passwordHash);
}
