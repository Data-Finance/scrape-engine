package it.datafinance.scrape_engine.domain.model;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserAccountTest {

  @Test
  void should_exposeAllFields_when_createdWithValidData() {
    UserAccount account = new UserAccount("1", "alice", "hash", Set.of(Role.USER, Role.ADMIN));

    assertSoftly(softly -> {
      softly.assertThat(account.id()).isEqualTo("1");
      softly.assertThat(account.username()).isEqualTo("alice");
      softly.assertThat(account.passwordHash()).isEqualTo("hash");
      softly.assertThat(account.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    });
  }

  @Test
  void should_allowNullId_when_accountIsNotPersistedYet() {
    UserAccount account = new UserAccount(null, "alice", "hash", Set.of(Role.USER));

    assertSoftly(softly -> softly.assertThat(account.id()).isNull());
  }

  @Test
  void should_copyRolesDefensively_when_sourceSetIsMutatedLater() {
    Set<Role> roles = new HashSet<>(Set.of(Role.USER));
    UserAccount account = new UserAccount("1", "alice", "hash", roles);

    roles.add(Role.ADMIN);

    assertSoftly(softly -> {
      softly.assertThat(account.roles()).containsExactly(Role.USER);
      softly
          .assertThatThrownBy(() -> account.roles().add(Role.ADMIN))
          .isInstanceOf(UnsupportedOperationException.class);
    });
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t"})
  void should_reject_when_usernameIsBlank(String username) {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new UserAccount("1", username, "hash", Set.of(Role.USER)))
        .withMessage("username must not be blank");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t"})
  void should_reject_when_passwordHashIsBlank(String passwordHash) {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new UserAccount("1", "alice", passwordHash, Set.of(Role.USER)))
        .withMessage("passwordHash must not be blank");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void should_reject_when_rolesAreMissing(Set<Role> roles) {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new UserAccount("1", "alice", "hash", roles))
        .withMessage("roles must not be empty");
  }
}
