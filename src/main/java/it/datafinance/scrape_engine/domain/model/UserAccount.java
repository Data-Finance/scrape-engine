package it.datafinance.scrape_engine.domain.model;

import java.util.Set;

public class UserAccount {

  private final String id;
  private final String username;
  private final String passwordHash;
  private final Set<Role> roles;

  public UserAccount(String id, String username, String passwordHash, Set<Role> roles) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username must not be blank");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("passwordHash must not be blank");
    }
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("roles must not be empty");
    }
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.roles = Set.copyOf(roles);
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Set<Role> getRoles() {
    return roles;
  }
}
