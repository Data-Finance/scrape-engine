package it.datafinance.scrape_engine.adapter.in.web;

import it.datafinance.scrape_engine.adapter.in.web.security.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
class AuthController {

  record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  record TokenResponse(String token) {}

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
  }

  @PostMapping("/token")
  TokenResponse token(@Valid @RequestBody LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    return new TokenResponse(tokenService.generate(authentication));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<String> badCredentials() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
  }
}
