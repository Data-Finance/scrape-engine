package it.datafinance.scrape_engine.adapter.in.web.security.service;

import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
class MongoUserDetailsService implements UserDetailsService {

  private final UserAccountRepository users;

  MongoUserDetailsService(UserAccountRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return users
        .findByUsername(username)
        .map(this::toUserDetails)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  private UserDetails toUserDetails(UserAccount account) {
    return User.withUsername(account.username())
        .password(account.passwordHash())
        .roles(account.roles().stream().map(Role::name).toArray(String[]::new))
        .build();
  }
}
