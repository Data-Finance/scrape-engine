package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One row of the document datasets embedded in the Eurizon documentation page. */
record EurizonDocument(
    @JsonProperty("ProductCode") String productCode,
    @JsonProperty("Name") String name,
    @JsonProperty("DocType") String docType,
    @JsonProperty("EncryptedFileId") String encryptedFileId) {}
