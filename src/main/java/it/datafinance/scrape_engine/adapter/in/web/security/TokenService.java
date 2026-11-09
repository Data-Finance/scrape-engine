package it.datafinance.scrape_engine.adapter.in.web.security;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class TokenService {

  static final Duration TOKEN_VALIDITY = Duration.ofHours(8);

  private final JwtEncoder encoder;

  TokenService(JwtEncoder encoder) {
    this.encoder = encoder;
  }

  public String generate(Authentication authentication) {
    Instant now = Instant.now();
    String scope =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .collect(Collectors.joining(" "));
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("scrape-engine")
            .issuedAt(now)
            .expiresAt(now.plus(TOKEN_VALIDITY))
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
