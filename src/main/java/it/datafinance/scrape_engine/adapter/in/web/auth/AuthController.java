package it.datafinance.scrape_engine.adapter.in.web.auth;

import it.datafinance.scrape_engine.adapter.in.web.auth.dto.LoginRequestDto;
import it.datafinance.scrape_engine.adapter.in.web.auth.dto.TokenResponseDto;
import it.datafinance.scrape_engine.application.port.in.LoginUseCase;
import jakarta.validation.Valid;
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
    var token = loginUseCase.login(request.username(), request.password());
    return new TokenResponseDto(token.value());
  }
}
