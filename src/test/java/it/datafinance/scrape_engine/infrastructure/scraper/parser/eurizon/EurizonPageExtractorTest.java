package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class EurizonPageExtractorTest {

  private final EurizonPageExtractor extractor =
      new EurizonPageExtractor(JsonMapper.builder().build());

  @Test
  void should_extractItalianAndLuxembourgDocuments_when_pageEmbedsBothDatasets() {
    List<EurizonDocument> documents = extractor.extract(EurizonFixtures.PAGE);

    assertSoftly(softly ->
        softly
            .assertThat(documents)
            .containsExactly(
                new EurizonDocument("11788-1", "Eurizon Investo Smart 10", "KID", "HHGGGE"),
                new EurizonDocument("11788-1", "Eurizon Investo Smart 10", "PROSPETTO", "HAAAAB"),
                new EurizonDocument(
                    "1029-2",
                    "Eurizon Fund - Equity Emerging Markets Smart Volatility R",
                    "KID",
                    "HHGGEB")));
  }

  @Test
  void should_keepTheOtherDataset_when_oneIsMissing() {
    String html =
        """
        <script>var dsLuxembourgFunds = [{"ProductCode":"1","Name":"Fund","DocType":"KID","EncryptedFileId":"AB"}];</script>
        """;

    List<EurizonDocument> documents = extractor.extract(html);

    assertSoftly(softly ->
        softly
            .assertThat(documents)
            .containsExactly(new EurizonDocument("1", "Fund", "KID", "AB")));
  }

  @Test
  void should_returnNoDocuments_when_datasetIsNotValidJson() {
    String html = "<script>var dsItalianFunds = [{\"ProductCode\": oops;</script>";

    assertSoftly(softly -> softly.assertThat(extractor.extract(html)).isEmpty());
  }

  @Test
  void should_returnNoDocuments_when_pageHasNoDataset() {
    assertSoftly(
        softly -> softly.assertThat(extractor.extract("<html><body></body></html>")).isEmpty());
  }
}
