package it.datafinance.scrape_engine.application.exception;

/** Thrown when a username/password pair does not match any account. */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Invalid username or password");
  }
}
