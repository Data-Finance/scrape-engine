package it.datafinance.scrape_engine.application.port.out;

import it.datafinance.scrape_engine.domain.model.AuthToken;
import it.datafinance.scrape_engine.domain.model.UserAccount;

public interface TokenIssuer {

  AuthToken issueFor(UserAccount account);
}
