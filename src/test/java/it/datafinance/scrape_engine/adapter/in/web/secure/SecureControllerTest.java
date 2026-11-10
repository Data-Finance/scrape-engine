package it.datafinance.scrape_engine.adapter.in.web.secure;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;

class SecureControllerTest {

  private final SecureController controller = new SecureController();

  @Test
  void should_greetAuthenticatedUser_when_principalIsPresent() {
    assertSoftly(
        softly -> softly.assertThat(controller.hello(() -> "alice")).isEqualTo("Hello, alice"));
  }
}
