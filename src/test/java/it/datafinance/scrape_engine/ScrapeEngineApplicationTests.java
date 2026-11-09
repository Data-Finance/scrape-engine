package it.datafinance.scrape_engine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

/**
 * Starts the whole app without needing .env or the real keys: MongoDB runs in a throwaway Docker
 * container and the JWT keys come from src/test/resources/certs (test-only keys).
 */
@SpringBootTest(
    properties = {
      "rsa.public-key=classpath:certs/public.pem",
      "rsa.private-key=classpath:certs/private.pem"
    })
@Testcontainers
class ScrapeEngineApplicationTests {

  @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

  @Test
  void contextLoads() {}
}
