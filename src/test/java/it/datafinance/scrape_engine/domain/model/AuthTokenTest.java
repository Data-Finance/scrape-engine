package it.datafinance.scrape_engine.domain.model;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AuthTokenTest {

  private static final Instant EXPIRES_AT = Instant.parse("2026-01-01T08:00:00Z");

  @Test
  void should_exposeValueAndExpiry_when_createdWithValidData() {
    AuthToken token = new AuthToken("jwt", EXPIRES_AT);

    assertSoftly(softly -> {
      softly.assertThat(token.value()).isEqualTo("jwt");
      softly.assertThat(token.expiresAt()).isEqualTo(EXPIRES_AT);
    });
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" "})
  void should_reject_when_valueIsBlank(String value) {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new AuthToken(value, EXPIRES_AT))
        .withMessage("value must not be blank");
  }

  @Test
  void should_reject_when_expiryIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new AuthToken("jwt", null))
        .withMessage("expiresAt must not be null");
  }
}
