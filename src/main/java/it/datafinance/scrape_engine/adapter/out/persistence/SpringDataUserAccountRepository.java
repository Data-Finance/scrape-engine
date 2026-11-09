package it.datafinance.scrape_engine.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataUserAccountRepository extends MongoRepository<UserAccountDocument, String> {

  Optional<UserAccountDocument> findByUsername(String username);
}
