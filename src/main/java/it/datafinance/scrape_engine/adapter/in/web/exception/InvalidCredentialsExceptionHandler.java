package it.datafinance.scrape_engine.adapter.in.web.exception;

import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates failed logins into {@code 401 Unauthorized} responses. */
@RestControllerAdvice
class InvalidCredentialsExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<String> handle(InvalidCredentialsException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
  }
}
