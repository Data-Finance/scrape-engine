package it.datafinance.scrape_engine.adapter.out.persistence.user;

import it.datafinance.scrape_engine.adapter.out.persistence.user.mapper.UserAccountDocumentMapper;
import it.datafinance.scrape_engine.adapter.out.persistence.user.repository.SpringDataUserAccountRepository;
import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class MongoUserAccountRepository implements UserAccountRepository {

  private final SpringDataUserAccountRepository repository;
  private final UserAccountDocumentMapper mapper;

  MongoUserAccountRepository(
      SpringDataUserAccountRepository repository, UserAccountDocumentMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<UserAccount> findByUsername(String username) {
    return repository.findByUsername(username).map(mapper::toDomain);
  }
}
