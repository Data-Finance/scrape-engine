package it.datafinance.scrape_engine.domain.model;

import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A user that can authenticate against the engine.
 *
 * @param id persistence identifier, {@code null} until the account has been stored
 */
public record UserAccount(
    @Nullable String id, String username, String passwordHash, Set<Role> roles) {

  public UserAccount {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username must not be blank");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("passwordHash must not be blank");
    }
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("roles must not be empty");
    }
    roles = Set.copyOf(roles);
  }
}
