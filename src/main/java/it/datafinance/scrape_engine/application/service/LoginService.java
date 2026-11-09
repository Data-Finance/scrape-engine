package it.datafinance.scrape_engine.application.service;

import it.datafinance.scrape_engine.application.ApplicationService;
import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import it.datafinance.scrape_engine.application.port.in.LoginUseCase;
import it.datafinance.scrape_engine.application.port.out.PasswordVerifier;
import it.datafinance.scrape_engine.application.port.out.TokenIssuer;
import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.AuthToken;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import org.jspecify.annotations.Nullable;

@ApplicationService
class LoginService implements LoginUseCase {

  private final UserAccountRepository users;
  private final PasswordVerifier passwordVerifier;
  private final TokenIssuer tokenIssuer;

  LoginService(
      UserAccountRepository users, PasswordVerifier passwordVerifier, TokenIssuer tokenIssuer) {
    this.users = users;
    this.passwordVerifier = passwordVerifier;
    this.tokenIssuer = tokenIssuer;
  }

  @Override
  public AuthToken login(String username, String password) {
    @Nullable UserAccount account = users.findByUsername(username).orElse(null);
    // Always verify, even for unknown users, so timing does not leak which usernames exist.
    boolean passwordMatches =
        passwordVerifier.matches(password, account == null ? null : account.passwordHash());
    if (account == null || !passwordMatches) {
      throw new InvalidCredentialsException();
    }
    return tokenIssuer.issueFor(account);
  }
}
