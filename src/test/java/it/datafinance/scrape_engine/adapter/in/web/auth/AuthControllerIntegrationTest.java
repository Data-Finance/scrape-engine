package it.datafinance.scrape_engine.adapter.in.web.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import it.datafinance.scrape_engine.application.port.out.UserAccountRepository;
import it.datafinance.scrape_engine.domain.model.Role;
import it.datafinance.scrape_engine.domain.model.UserAccount;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class AuthControllerIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired PasswordEncoder passwordEncoder;
  @MockitoBean UserAccountRepository users;

  @BeforeEach
  void seedUser() {
    given(users.findByUsername(anyString())).willReturn(Optional.empty());
    given(users.findByUsername("alice"))
        .willReturn(
            Optional.of(
                new UserAccount(
                    "1", "alice", passwordEncoder.encode("secret"), Set.of(Role.USER))));
  }

  @Test
  void should_returnUsableToken_when_credentialsAreValid() throws Exception {
    String body =
        mvc.perform(
                post("/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String token = JsonPath.read(body, "$.token");

    mvc.perform(get("/api/secure/hello").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, alice"));
  }

  @Test
  void should_returnUnauthorized_when_passwordIsWrong() throws Exception {
    mvc.perform(
            post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"nope\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_returnUnauthorized_when_userDoesNotExist() throws Exception {
    mvc.perform(
            post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"bob\",\"password\":\"secret\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_returnUnauthorized_when_secureEndpointIsCalledWithoutToken() throws Exception {
    mvc.perform(get("/api/secure/hello")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_returnBadRequest_when_usernameIsBlank() throws Exception {
    mvc.perform(
            post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"secret\"}"))
        .andExpect(status().isBadRequest());
  }
}
