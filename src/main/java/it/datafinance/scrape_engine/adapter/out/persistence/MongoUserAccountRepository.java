package it.datafinance.scrape_engine.adapter.out.persistence;

import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class MongoUserAccountRepository implements UserAccountRepository {

  private final SpringDataUserAccountRepository repository;

  MongoUserAccountRepository(SpringDataUserAccountRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<UserAccount> findByUsername(String username) {
    return repository.findByUsername(username).map(MongoUserAccountRepository::toDomain);
  }

  private static UserAccount toDomain(UserAccountDocument doc) {
    return new UserAccount(doc.id(), doc.username(), doc.passwordHash(), doc.roles());
  }
}
