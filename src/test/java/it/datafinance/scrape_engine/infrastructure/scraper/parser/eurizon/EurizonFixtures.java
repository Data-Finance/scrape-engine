package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

/** A cut-down copy of the Eurizon documentation page, with the same script layout. */
final class EurizonFixtures {

  static final String PAGE =
      """
      <html><head><title>Documentazione d'offerta - Eurizon</title></head><body>
      <script>
          var dsItalianFunds = [{"ProductCode":"11788-1","Class":"NC","FundsSection":"Eurizon Investo Smart 10","Name":"Eurizon Investo Smart 10","DocType":"KID","System":"Sistema Eurizon Investo Smart","DocTypeDescription":"Documento contenente le informazioni chiave (KID)","ViewOrder":35.0,"FileId":776664,"EncryptedFileId":"HHGGGE","Category":"Documentazione legale","Company":"Eurizon Capital SGR S.p.A."},{"ProductCode":"11788-1","Class":"NC","FundsSection":"Eurizon Investo Smart 10","Name":"Eurizon Investo Smart 10","DocType":"PROSPETTO","System":"Sistema Eurizon Investo Smart","DocTypeDescription":"Prospetto","ViewOrder":10.0,"FileId":700001,"EncryptedFileId":"HAAAAB","Category":"Documentazione legale","Company":"Eurizon Capital SGR S.p.A."}];
          var dsItalianDocumentTypes = {"KID":"Documento contenente le informazioni chiave (KID)","PROSPETTO":"Prospetto"};
          var labelNoResults = "Nessun risultato trovato";
      </script>
      <div class="component-content row"><input id="pffd-it-doctype-auto-input" type="text" /></div>
      <script>
          var dsLuxembourgFunds = [{"ProductCode":"1029-2","Class":"R","FundsSection":"Eurizon Fund - Equity Emerging Markets Smart Volatility R","Name":"Eurizon Fund - Equity Emerging Markets Smart Volatility R","DocType":"KID","System":"Eurizon Fund","DocTypeDescription":"Documento contenente le informazioni chiave (KID)","ViewOrder":35.0,"FileId":776641,"EncryptedFileId":"HHGGEB","Category":"Documentazione legale","Company":"Eurizon Capital SGR S.p.A. - Luxembourg Branch"}];
          var dsLuxembourgDocumentTypes = {"KID":"Documento contenente le informazioni chiave (KID)"};
      </script>
      </body></html>
      """;

  private EurizonFixtures() {}
}
