package it.datafinance.scrape_engine.adapter.in.web.auth;

import it.datafinance.scrape_engine.adapter.in.web.auth.dto.LoginRequestDto;
import it.datafinance.scrape_engine.adapter.in.web.auth.dto.TokenResponseDto;
import it.datafinance.scrape_engine.application.exception.InvalidCredentialsException;
import it.datafinance.scrape_engine.application.port.in.LoginCommand;
import it.datafinance.scrape_engine.application.port.in.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
class AuthController {

  private final LoginUseCase loginUseCase;

  AuthController(LoginUseCase loginUseCase) {
    this.loginUseCase = loginUseCase;
  }

  @PostMapping("/token")
  TokenResponseDto token(@Valid @RequestBody LoginRequestDto request) {
    var token = loginUseCase.login(new LoginCommand(request.username(), request.password()));
    return new TokenResponseDto(token.value());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<String> badCredentials(InvalidCredentialsException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
  }
}
