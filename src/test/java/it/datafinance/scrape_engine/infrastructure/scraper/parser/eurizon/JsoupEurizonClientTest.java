package it.datafinance.scrape_engine.infrastructure.scraper.parser.eurizon;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import it.datafinance.scrape_engine.infrastructure.scraper.model.ScrapedFile;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsoupEurizonClientTest {

  private static final byte[] PDF = "%PDF-1.5 content".getBytes(StandardCharsets.UTF_8);

  private final JsoupEurizonClient client = new JsoupEurizonClient("test-agent");
  private HttpServer server;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    // Like the real page: redirects to itself until the cookie it sets comes back
    server.createContext(
        "/page",
        exchange -> {
          String cookie = exchange.getRequestHeaders().getFirst("Cookie");
          if (cookie == null || !cookie.contains("lang=it-IT")) {
            exchange.getResponseHeaders().add("Set-Cookie", "lang=it-IT; path=/");
            exchange.getResponseHeaders().add("Location", "/page");
            respond(exchange, 302, "text/html", new byte[0]);
          } else {
            String agent = exchange.getRequestHeaders().getFirst("User-Agent");
            respond(
                exchange,
                200,
                "text/html; charset=utf-8",
                ("<html>" + agent + "</html>").getBytes(StandardCharsets.UTF_8));
          }
        });
    server.createContext(
        "/kid",
        exchange -> {
          exchange
              .getResponseHeaders()
              .add("Content-Disposition", "attachment; filename=55_11788-1_IT0005506610.pdf");
          respond(exchange, 200, "application/pdf", PDF);
        });
    server.createContext("/unnamed", exchange -> respond(exchange, 200, "application/pdf", PDF));
    server.createContext(
        "/not-a-pdf",
        exchange -> {
          exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=a.pdf");
          respond(exchange, 200, "text/html", "<html></html>".getBytes(StandardCharsets.UTF_8));
        });
    server.createContext(
        "/denied",
        exchange ->
            respond(exchange, 403, "text/html", "Access Denied".getBytes(StandardCharsets.UTF_8)));
    server.createContext(
        "/broken",
        exchange -> respond(exchange, 500, "text/html", "error".getBytes(StandardCharsets.UTF_8)));
    server.start();
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  @Test
  void should_returnPageHtml_when_pageRedirectsToItselfToSetCookies() throws IOException {
    String html = client.fetchPage(url("/page"));

    assertSoftly(softly -> softly.assertThat(html).isEqualTo("<html>test-agent</html>"));
  }

  @Test
  void should_returnFileNamedAfterContentDisposition_when_downloadingPdf() throws IOException {
    ScrapedFile file = client.download(url("/kid"));

    assertSoftly(softly -> {
      softly.assertThat(file.fileName()).isEqualTo("55_11788-1_IT0005506610.pdf");
      softly.assertThat(file.data()).isEqualTo(PDF);
    });
  }

  @Test
  void should_throwAccessDenied_when_serverAnswers403() {
    assertSoftly(softly -> {
      softly
          .assertThatThrownBy(() -> client.download(url("/denied")))
          .isInstanceOf(EurizonAccessDeniedException.class);
      softly
          .assertThatThrownBy(() -> client.fetchPage(url("/denied")))
          .isInstanceOf(EurizonAccessDeniedException.class);
    });
  }

  @Test
  void should_throwIOException_when_responseIsNotAUsablePdf() {
    assertSoftly(softly -> {
      softly
          .assertThatThrownBy(() -> client.download(url("/broken")))
          .isExactlyInstanceOf(IOException.class)
          .hasMessageContaining("HTTP 500");
      softly
          .assertThatThrownBy(() -> client.download(url("/not-a-pdf")))
          .isExactlyInstanceOf(IOException.class)
          .hasMessageContaining("Expected a PDF");
      softly
          .assertThatThrownBy(() -> client.download(url("/unnamed")))
          .isExactlyInstanceOf(IOException.class)
          .hasMessageContaining("No file name");
    });
  }

  private URL url(String path) throws IOException {
    return URI.create("http://localhost:" + server.getAddress().getPort() + path).toURL();
  }

  private static void respond(HttpExchange exchange, int status, String contentType, byte[] body)
      throws IOException {
    exchange.getResponseHeaders().add("Content-Type", contentType);
    exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
    if (body.length > 0) {
      exchange.getResponseBody().write(body);
    }
    exchange.close();
  }
}
