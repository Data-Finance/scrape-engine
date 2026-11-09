package it.datafinance.scrape_engine.adapter.in.web.auth;

import it.datafinance.scrape_engine.adapter.in.web.auth.dto.LoginRequestDto;
import it.datafinance.scrape_engine.adapter.in.web.auth.dto.TokenResponseDto;
import it.datafinance.scrape_engine.adapter.in.web.security.service.TokenService;
import jakarta.validation.Valid;
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

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
  }

  @PostMapping("/token")
  TokenResponseDto token(@Valid @RequestBody LoginRequestDto request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    return new TokenResponseDto(tokenService.generate(authentication));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<String> badCredentials() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
  }
}
