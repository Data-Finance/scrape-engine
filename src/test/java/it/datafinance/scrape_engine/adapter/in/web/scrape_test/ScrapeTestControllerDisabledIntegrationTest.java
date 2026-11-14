package it.datafinance.scrape_engine.adapter.in.web.scrape_test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.datafinance.scrape_engine.support.TestRsaKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "scraper.test-api.enabled=false")
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class ScrapeTestControllerDisabledIntegrationTest {

  @DynamicPropertySource
  static void rsaKeys(DynamicPropertyRegistry registry) {
    TestRsaKeys.register(registry);
  }

  @Autowired MockMvc mvc;

  @Test
  void should_notExposeEndpoint_when_testApiIsDisabled() throws Exception {
    mvc.perform(get("/scrape-test/eurizon").param("url", "https://example.com/documents"))
        .andExpect(status().isNotFound());
  }
}
