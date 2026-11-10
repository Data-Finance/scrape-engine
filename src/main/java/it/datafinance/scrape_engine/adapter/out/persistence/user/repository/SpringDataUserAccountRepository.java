package it.datafinance.scrape_engine.adapter.out.persistence.user.repository;

import it.datafinance.scrape_engine.adapter.out.persistence.user.document.UserAccountDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataUserAccountRepository
    extends MongoRepository<UserAccountDocument, String> {

  Optional<UserAccountDocument> findByUsername(String username);
}
