package it.datafinance.scrape_engine.adapter.out.persistence.user.mapper;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import it.datafinance.scrape_engine.adapter.out.persistence.user.document.UserAccountDocument;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserAccountDocumentMapperTest {

  private final UserAccountDocumentMapper mapper = new UserAccountDocumentMapper();

  @Test
  void should_mapAllFieldsToDomain_when_documentIsValid() {
    UserAccountDocument document =
        new UserAccountDocument("1", "alice", "hash", Set.of("USER", "ADMIN"));

    UserAccount account = mapper.toDomain(document);

    assertSoftly(softly -> {
      softly.assertThat(account.id()).isEqualTo("1");
      softly.assertThat(account.username()).isEqualTo("alice");
      softly.assertThat(account.passwordHash()).isEqualTo("hash");
      softly.assertThat(account.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    });
  }

  @Test
  void should_fail_when_documentContainsUnknownRole() {
    UserAccountDocument document = new UserAccountDocument("1", "alice", "hash", Set.of("ROOT"));

    assertThatIllegalArgumentException().isThrownBy(() -> mapper.toDomain(document));
  }
}
