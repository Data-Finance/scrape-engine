package it.datafinance.scrape_engine.adapter.out.persistence;

import it.datafinance.scrape_engine.domain.model.Role;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
record UserAccountDocument(
    @Id String id, @Indexed(unique = true) String username, String passwordHash, Set<Role> roles) {}
