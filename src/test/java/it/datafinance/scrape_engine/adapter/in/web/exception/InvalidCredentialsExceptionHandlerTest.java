package it.datafinance.scrape_engine.adapter.in.web.exception;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class InvalidCredentialsExceptionHandlerTest {

  private final InvalidCredentialsExceptionHandler handler =
      new InvalidCredentialsExceptionHandler();

  @Test
  void should_returnUnauthorizedWithMessage_when_credentialsAreInvalid() {
    ResponseEntity<String> response = handler.handle(new InvalidCredentialsException());

    assertSoftly(softly -> {
      softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      softly.assertThat(response.getBody()).isEqualTo("Invalid username or password");
    });
  }
}
