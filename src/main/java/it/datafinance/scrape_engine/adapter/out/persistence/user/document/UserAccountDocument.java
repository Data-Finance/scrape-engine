package it.datafinance.scrape_engine.adapter.out.persistence.user.document;

import it.datafinance.scrape_engine.domain.model.Role;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record UserAccountDocument(
    @Id @Nullable String id,
    @Indexed(unique = true) String username,
    String passwordHash,
    Set<Role> roles) {}
