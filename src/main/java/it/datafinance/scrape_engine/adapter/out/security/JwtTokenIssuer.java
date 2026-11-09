package it.datafinance.scrape_engine.adapter.out.security;

import it.datafinance.scrape_engine.application.port.out.TokenIssuer;
import it.datafinance.scrape_engine.domain.model.AuthToken;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class JwtTokenIssuer implements TokenIssuer {

  static final Duration TOKEN_VALIDITY = Duration.ofHours(8);
  static final String ISSUER = "scrape-engine";

  private final JwtEncoder encoder;

  JwtTokenIssuer(JwtEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  public AuthToken issueFor(UserAccount account) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(TOKEN_VALIDITY);
    String scope =
        account.roles().stream()
            .map(role -> "ROLE_" + role.name())
            .collect(Collectors.joining(" "));
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(account.username())
            .claim("scope", scope)
            .build();
    String value = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    return new AuthToken(value, expiresAt);
  }
}
