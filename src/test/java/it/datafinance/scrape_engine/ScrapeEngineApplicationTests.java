package it.datafinance.scrape_engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Execution(ExecutionMode.SAME_THREAD)
class ScrapeEngineApplicationTests {

  @Test
  void should_loadApplicationContext() {}
}
