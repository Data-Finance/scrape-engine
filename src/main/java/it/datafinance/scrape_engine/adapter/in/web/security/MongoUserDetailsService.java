package it.datafinance.scrape_engine.adapter.in.web.security;

import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.Role;
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
        .map(account ->
            User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getRoles().stream().map(Role::name).toArray(String[]::new))
                .build())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
