package it.datafinance.scrape_engine.adapter.out.security;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class BCryptPasswordVerifierTest {

  // Lowest allowed BCrypt strength keeps the tests fast.
  private final PasswordEncoder encoder = spy(new BCryptPasswordEncoder(4));
  private final BCryptPasswordVerifier verifier = new BCryptPasswordVerifier(encoder);

  @Test
  void should_matchOnlyTheOriginalPassword_when_hashIsPresent() {
    String hash = encoder.encode("secret");

    assertSoftly(softly -> {
      softly.assertThat(verifier.matches("secret", hash)).isTrue();
      softly.assertThat(verifier.matches("wrong", hash)).isFalse();
    });
  }

  @Test
  void should_returnFalse_when_hashIsNull() {
    assertSoftly(softly -> softly.assertThat(verifier.matches("secret", null)).isFalse());
  }

  @Test
  void should_runDummyComparison_when_hashIsNull() {
    verifier.matches("secret", null);

    verify(encoder).matches(eq("secret"), anyString());
  }
}
