package it.datafinance.scrape_engine.application.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import it.datafinance.scrape_engine.application.port.out.PasswordVerifier;
import it.datafinance.scrape_engine.application.port.out.TokenIssuer;
import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.AuthToken;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  private static final UserAccount ALICE =
      new UserAccount("1", "alice", "stored-hash", Set.of(Role.USER));
  private static final AuthToken TOKEN = new AuthToken("jwt", Instant.EPOCH);

  @Mock private UserAccountRepository users;
  @Mock private PasswordVerifier passwordVerifier;
  @Mock private TokenIssuer tokenIssuer;

  private LoginService service;

  @BeforeEach
  void setUp() {
    service = new LoginService(users, passwordVerifier, tokenIssuer);
  }

  @Test
  void should_issueToken_when_credentialsAreValid() {
    given(users.findByUsername("alice")).willReturn(Optional.of(ALICE));
    given(passwordVerifier.matches("secret", "stored-hash")).willReturn(true);
    given(tokenIssuer.issueFor(ALICE)).willReturn(TOKEN);

    AuthToken result = service.login("alice", "secret");

    assertSoftly(softly -> softly.assertThat(result).isEqualTo(TOKEN));
  }

  @Test
  void should_throwInvalidCredentials_when_passwordIsWrong() {
    given(users.findByUsername("alice")).willReturn(Optional.of(ALICE));
    given(passwordVerifier.matches("wrong", "stored-hash")).willReturn(false);

    assertSoftly(softly ->
        softly
            .assertThatThrownBy(() -> service.login("alice", "wrong"))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid username or password"));
    then(tokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void should_throwInvalidCredentials_when_userDoesNotExist() {
    given(users.findByUsername("bob")).willReturn(Optional.empty());

    assertSoftly(softly ->
        softly
            .assertThatThrownBy(() -> service.login("bob", "secret"))
            .isInstanceOf(InvalidCredentialsException.class));
    then(tokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void should_stillVerifyPassword_when_userDoesNotExist() {
    given(users.findByUsername("bob")).willReturn(Optional.empty());

    try {
      service.login("bob", "secret");
    } catch (InvalidCredentialsException expected) {
      // asserted in should_throwInvalidCredentials_when_userDoesNotExist
    }

    then(passwordVerifier).should().matches(any(), isNull());
  }
}
