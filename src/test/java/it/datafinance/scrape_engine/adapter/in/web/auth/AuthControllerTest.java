package it.datafinance.scrape_engine.adapter.in.web.auth;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import it.datafinance.scrape_engine.adapter.in.web.auth.dto.LoginRequestDto;
import it.datafinance.scrape_engine.adapter.in.web.auth.dto.TokenResponseDto;
import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import it.datafinance.scrape_engine.application.port.in.LoginUseCase;
import it.datafinance.scrape_engine.domain.model.AuthToken;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private LoginUseCase loginUseCase;

  private AuthController controller;

  @BeforeEach
  void setUp() {
    controller = new AuthController(loginUseCase);
  }

  @Test
  void should_returnTokenValue_when_loginSucceeds() {
    given(loginUseCase.login("alice", "secret")).willReturn(new AuthToken("jwt", Instant.EPOCH));

    TokenResponseDto response = controller.token(new LoginRequestDto("alice", "secret"));

    assertSoftly(softly -> softly.assertThat(response.token()).isEqualTo("jwt"));
  }

  @Test
  void should_propagateInvalidCredentials_when_loginFails() {
    given(loginUseCase.login("alice", "wrong")).willThrow(new InvalidCredentialsException());

    assertSoftly(softly ->
        softly
            .assertThatThrownBy(() -> controller.token(new LoginRequestDto("alice", "wrong")))
            .isInstanceOf(InvalidCredentialsException.class));
  }
}
