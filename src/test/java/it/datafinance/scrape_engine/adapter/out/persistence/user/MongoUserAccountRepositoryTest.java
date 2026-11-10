package it.datafinance.scrape_engine.adapter.out.persistence.user;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import it.datafinance.scrape_engine.adapter.out.persistence.user.document.UserAccountDocument;
import it.datafinance.scrape_engine.adapter.out.persistence.user.mapper.UserAccountDocumentMapper;
import it.datafinance.scrape_engine.adapter.out.persistence.user.repository.SpringDataUserAccountRepository;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoUserAccountRepositoryTest {

  @Mock private SpringDataUserAccountRepository springDataRepository;

  private MongoUserAccountRepository repository;

  @BeforeEach
  void setUp() {
    repository =
        new MongoUserAccountRepository(springDataRepository, new UserAccountDocumentMapper());
  }

  @Test
  void should_returnMappedAccount_when_usernameExists() {
    given(springDataRepository.findByUsername("alice"))
        .willReturn(Optional.of(new UserAccountDocument("1", "alice", "hash", Set.of("USER"))));

    Optional<UserAccount> result = repository.findByUsername("alice");

    assertSoftly(softly ->
        softly
            .assertThat(result)
            .contains(new UserAccount("1", "alice", "hash", Set.of(Role.USER))));
  }

  @Test
  void should_returnEmpty_when_usernameDoesNotExist() {
    given(springDataRepository.findByUsername("bob")).willReturn(Optional.empty());

    assertSoftly(softly -> softly.assertThat(repository.findByUsername("bob")).isEmpty());
  }
}
