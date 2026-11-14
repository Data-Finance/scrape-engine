package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the documents listed in the Eurizon documentation page. The page does not render them as
 * HTML: it embeds them as JSON arrays assigned to JavaScript variables, one for the Italian funds
 * and one for the Luxembourg funds.
 */
@Component
class EurizonPageExtractor {

  private static final Logger log = LoggerFactory.getLogger(EurizonPageExtractor.class);

  private static final List<String> DATASETS = List.of("dsItalianFunds", "dsLuxembourgFunds");

  private final ObjectReader reader;

  EurizonPageExtractor(JsonMapper jsonMapper) {
    // The array is followed by the rest of the script, so trailing content must be tolerated.
    this.reader =
        jsonMapper
            .readerForListOf(EurizonDocument.class)
            .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
  }

  List<EurizonDocument> extract(String html) {
    List<String> scripts = Jsoup.parse(html).select("script").stream().map(Element::data).toList();
    List<EurizonDocument> documents = new ArrayList<>();
    for (String dataset : DATASETS) {
      documents.addAll(extractDataset(scripts, dataset));
    }
    return documents;
  }

  private List<EurizonDocument> extractDataset(List<String> scripts, String dataset) {
    Pattern assignment = Pattern.compile("\\bvar\\s+" + dataset + "\\s*=\\s*(?=\\[)");
    for (String script : scripts) {
      Matcher matcher = assignment.matcher(script);
      if (matcher.find()) {
        try {
          List<EurizonDocument> documents = reader.readValue(script.substring(matcher.end()));
          log.debug("Eurizon dataset {} holds {} documents", dataset, documents.size());
          return documents;
        } catch (JacksonException e) {
          log.warn("Eurizon dataset {} is not valid JSON", dataset, e);
          return List.of();
        }
      }
    }
    log.warn("Eurizon dataset {} not found in the page", dataset);
    return List.of();
  }
}
