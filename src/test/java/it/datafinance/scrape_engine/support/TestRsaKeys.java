package it.datafinance.scrape_engine.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Throwaway RSA key pair for Spring context tests, so they don't need the git-ignored {@code
 * certs/} folder. The pair is generated once per JVM and written to temp PEM files.
 */
public final class TestRsaKeys {

  private static final Paths PATHS = generate();

  private TestRsaKeys() {}

  public static void register(DynamicPropertyRegistry registry) {
    registry.add("rsa.public-key", () -> PATHS.publicKey().toUri().toString());
    registry.add("rsa.private-key", () -> PATHS.privateKey().toUri().toString());
  }

  private static Paths generate() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      Path dir = Files.createTempDirectory("scrape-engine-test-keys");
      Path publicKey =
          Files.writeString(
              dir.resolve("public.pem"), pem("PUBLIC KEY", keyPair.getPublic().getEncoded()));
      Path privateKey =
          Files.writeString(
              dir.resolve("private.pem"), pem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
      dir.toFile().deleteOnExit();
      publicKey.toFile().deleteOnExit();
      privateKey.toFile().deleteOnExit();
      return new Paths(publicKey, privateKey);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String pem(String type, byte[] der) {
    String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
    return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
  }

  private record Paths(Path publicKey, Path privateKey) {}
}
