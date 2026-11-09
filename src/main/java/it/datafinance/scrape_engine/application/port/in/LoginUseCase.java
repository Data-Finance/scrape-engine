package it.datafinance.scrape_engine.application.port.in;

import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import it.datafinance.scrape_engine.domain.model.AuthToken;

/** Authenticates a user with username and password and issues an access token. */
public interface LoginUseCase {

  /**
   * @throws InvalidCredentialsException if the user does not exist or the password is wrong
   */
  AuthToken login(LoginCommand command);
}
