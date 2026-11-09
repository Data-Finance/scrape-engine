package it.datafinance.scrape_engine.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import it.datafinance.scrape_engine.application.port.out.PasswordVerifier;
import it.datafinance.scrape_engine.application.port.out.TokenIssuer;
import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.AuthToken;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LoginServiceTest {

  private static final UserAccount ALICE =
      new UserAccount("1", "alice", "hash:secret", Set.of(Role.USER));
  private static final AuthToken TOKEN = new AuthToken("jwt", Instant.EPOCH);

  private final UserAccountRepository users =
      username -> Optional.of(ALICE).filter(a -> a.username().equals(username));
  private final List<String> verifiedHashes = new ArrayList<>();
  private final PasswordVerifier verifier =
      (raw, hash) -> {
        verifiedHashes.add(String.valueOf(hash));
        return ("hash:" + raw).equals(hash);
      };
  private final TokenIssuer issuer = account -> TOKEN;

  private final LoginService service = new LoginService(users, verifier, issuer);

  @Test
  void issuesTokenForValidCredentials() {
    assertThat(service.login("alice", "secret")).isEqualTo(TOKEN);
  }

  @Test
  void rejectsWrongPassword() {
    assertThatThrownBy(() -> service.login("alice", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void rejectsUnknownUserButStillVerifiesPassword() {
    assertThatThrownBy(() -> service.login("bob", "secret"))
        .isInstanceOf(InvalidCredentialsException.class);
    assertThat(verifiedHashes).containsExactly("null");
  }
}
