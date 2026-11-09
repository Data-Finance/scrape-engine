package it.datafinance.scrape_engine.adapter.in.web.security.service;

import org.springframework.security.core.Authentication;

/** Issues access tokens for authenticated principals. */
public interface TokenService {

  String generate(Authentication authentication);
}
