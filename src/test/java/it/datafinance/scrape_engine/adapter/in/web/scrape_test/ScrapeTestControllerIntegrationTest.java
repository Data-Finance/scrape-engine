package it.datafinance.scrape_engine.adapter.in.web.scrape_test;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.datafinance.scrape_engine.infrastructure.scraper.BaseScraperService;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeRequest;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapeResult;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedFile;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedProduct;
import it.datafinance.scrape_engine.support.TestRsaKeys;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "scraper.test-api.enabled=true")
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class ScrapeTestControllerIntegrationTest {

  private static final String SITE_URL = "https://example.com/documents";

  @DynamicPropertySource
  static void rsaKeys(DynamicPropertyRegistry registry) {
    TestRsaKeys.register(registry);
  }

  @Autowired MockMvc mvc;
  @MockitoBean BaseScraperService scraperService;

  @Test
  void should_returnScrapedProductsWithoutAuthentication_when_scrapeSucceeds() throws Exception {
    given(scraperService.execute(request()))
        .willReturn(
            new ScrapeResult.Success(
                List.of(
                    new ScrapedProduct(
                        "Fund A",
                        "IT0005506610",
                        new ScrapedFile("55_1_IT0005506610.pdf", new byte[] {1, 2, 3})))));

    mvc.perform(get("/scrape-test/eurizon").param("url", SITE_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.productCount").value(1))
        .andExpect(jsonPath("$.products[0].productName").value("Fund A"))
        .andExpect(jsonPath("$.products[0].isin").value("IT0005506610"))
        .andExpect(jsonPath("$.products[0].fileName").value("55_1_IT0005506610.pdf"))
        .andExpect(jsonPath("$.products[0].sizeBytes").value(3))
        .andExpect(jsonPath("$.products[0].data").doesNotExist());
  }

  @Test
  void should_returnBadGatewayWithErrorCode_when_scrapeFails() throws Exception {
    given(scraperService.execute(request()))
        .willReturn(new ScrapeResult.Failed("Eurizon denied access", "EURIZON.ACCESS_DENIED"));

    mvc.perform(get("/scrape-test/eurizon").param("url", SITE_URL))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value("EURIZON.ACCESS_DENIED"))
        .andExpect(jsonPath("$.errorMessage").value("Eurizon denied access"))
        .andExpect(jsonPath("$.productCount").value(0));
  }

  @Test
  void should_returnBadRequest_when_urlIsMissing() throws Exception {
    mvc.perform(get("/scrape-test/eurizon")).andExpect(status().isBadRequest());
  }

  private static ScrapeRequest request() throws Exception {
    return new ScrapeRequest("eurizon", URI.create(SITE_URL).toURL());
  }
}
