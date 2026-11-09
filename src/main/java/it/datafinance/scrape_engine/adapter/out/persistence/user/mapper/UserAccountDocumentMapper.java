package it.datafinance.scrape_engine.adapter.out.persistence.user.mapper;

import it.datafinance.scrape_engine.adapter.out.persistence.user.document.UserAccountDocument;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserAccountDocumentMapper {

  public UserAccount toDomain(UserAccountDocument document) {
    return new UserAccount(
        document.id(), document.username(), document.passwordHash(), document.roles());
  }
}
