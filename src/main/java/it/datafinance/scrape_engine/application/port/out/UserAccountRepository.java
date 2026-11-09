package it.datafinance.scrape_engine.application.port.out;

import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {

  Optional<UserAccount> findByUsername(String username);
}
