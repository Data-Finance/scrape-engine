package it.datafinance.scrape_engine.adapter.out.persistence.user.document;

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo representation of a user. Roles are stored as plain strings, decoupled from the domain. */
@Document("users")
public record UserAccountDocument(
    @Id @Nullable String id,
    @Indexed(unique = true) String username,
    String passwordHash,
    Set<String> roles) {}
