package it.datafinance.scrape_engine.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank String username, @NotBlank String password) {}
