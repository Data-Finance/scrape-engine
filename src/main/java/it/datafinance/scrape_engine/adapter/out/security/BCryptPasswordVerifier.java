package it.datafinance.scrape_engine.adapter.out.security;

import it.datafinance.scrape_engine.application.port.out.PasswordVerifier;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptPasswordVerifier implements PasswordVerifier {

  private final PasswordEncoder passwordEncoder;
  private final String dummyHash;

  BCryptPasswordVerifier(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
    this.dummyHash = passwordEncoder.encode("timing-attack-mitigation");
  }

  @Override
  public boolean matches(String rawPassword, @Nullable String passwordHash) {
    if (passwordHash == null) {
      passwordEncoder.matches(rawPassword, dummyHash);
      return false;
    }
    return passwordEncoder.matches(rawPassword, passwordHash);
  }
}
