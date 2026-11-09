package it.datafinance.scrape_engine;

import it.datafinance.scrape_engine.support.TestRsaKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Execution(ExecutionMode.SAME_THREAD)
class ScrapeEngineApplicationTests {

  @DynamicPropertySource
  static void rsaKeys(DynamicPropertyRegistry registry) {
    TestRsaKeys.register(registry);
  }

  @Test
  void should_loadApplicationContext() {}
}
