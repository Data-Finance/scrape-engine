package it.datafinance.scrape_engine.adapter.out.security;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import it.datafinance.scrape_engine.domain.model.AuthToken;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenIssuerTest {

  private static JwtTokenIssuer issuer;
  private static JwtDecoder decoder;

  @BeforeAll
  static void setUpKeys() throws NoSuchAlgorithmException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAKey jwk =
        new RSAKey.Builder(publicKey).privateKey((RSAPrivateKey) keyPair.getPrivate()).build();
    issuer = new JwtTokenIssuer(new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk))));
    decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
  }

  @Test
  void should_issueSignedJwtWithExpectedClaims_when_accountIsValid() {
    UserAccount account = new UserAccount("1", "alice", "hash", Set.of(Role.USER));
    Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    AuthToken token = issuer.issueFor(account);
    Jwt jwt = decoder.decode(token.value());

    assertSoftly(softly -> {
      softly.assertThat(jwt.getSubject()).isEqualTo("alice");
      softly.assertThat(jwt.getClaimAsString("iss")).isEqualTo(JwtTokenIssuer.ISSUER);
      softly.assertThat(jwt.getClaimAsString("scope")).isEqualTo("ROLE_USER");
      softly.assertThat(jwt.getIssuedAt()).isAfterOrEqualTo(before);
      softly
          .assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()))
          .isEqualTo(JwtTokenIssuer.TOKEN_VALIDITY);
      softly
          .assertThat(token.expiresAt().truncatedTo(ChronoUnit.SECONDS))
          .isEqualTo(jwt.getExpiresAt());
    });
  }

  @Test
  void should_listEveryRoleInScope_when_accountHasMultipleRoles() {
    UserAccount account = new UserAccount("1", "admin", "hash", Set.of(Role.USER, Role.ADMIN));

    Jwt jwt = decoder.decode(issuer.issueFor(account).value());

    assertSoftly(softly ->
        softly
            .assertThat(jwt.getClaimAsString("scope").split(" "))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN"));
  }
}
