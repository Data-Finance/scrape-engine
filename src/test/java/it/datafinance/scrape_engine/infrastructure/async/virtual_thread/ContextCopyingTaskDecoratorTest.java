package it.datafinance.scrape_engine.infrastructure.async.virtual_thread;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class ContextCopyingTaskDecoratorTest {

  private final ContextCopyingTaskDecorator decorator = new ContextCopyingTaskDecorator();

  @AfterEach
  void clearCallerContext() {
    MDC.clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_propagateMdcAndSecurityContext_when_taskRunsOnAnotherThread() throws Exception {
    Authentication authentication = new TestingAuthenticationToken("user", "password");
    MDC.put("traceId", "abc-123");
    SecurityContextHolder.getContext().setAuthentication(authentication);

    AtomicReference<Map<String, String>> mdcSeen = new AtomicReference<>();
    AtomicReference<Authentication> authenticationSeen = new AtomicReference<>();
    Runnable decorated =
        decorator.decorate(() -> {
          mdcSeen.set(MDC.getCopyOfContextMap());
          authenticationSeen.set(SecurityContextHolder.getContext().getAuthentication());
        });

    runOnNewThread(decorated);

    assertSoftly(softly -> {
      softly.assertThat(mdcSeen.get()).containsExactlyEntriesOf(Map.of("traceId", "abc-123"));
      softly.assertThat(authenticationSeen.get()).isSameAs(authentication);
    });
  }

  @Test
  void should_clearContexts_when_taskCompletes() throws Exception {
    MDC.put("traceId", "abc-123");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("user", "password"));

    Runnable decorated = decorator.decorate(() -> {});
    AtomicReference<Map<String, String>> mdcAfter = new AtomicReference<>();
    AtomicReference<SecurityContext> securityAfter = new AtomicReference<>();

    runOnNewThread(() -> {
      decorated.run();
      mdcAfter.set(MDC.getCopyOfContextMap());
      securityAfter.set(SecurityContextHolder.getContext());
    });

    assertSoftly(softly -> {
      softly.assertThat(mdcAfter.get()).isNullOrEmpty();
      softly.assertThat(securityAfter.get().getAuthentication()).isNull();
    });
  }

  @Test
  void should_clearContexts_when_taskThrows() throws Exception {
    MDC.put("traceId", "abc-123");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("user", "password"));

    Runnable decorated =
        decorator.decorate(() -> {
          throw new IllegalStateException("boom");
        });
    AtomicReference<Throwable> thrown = new AtomicReference<>();
    AtomicReference<Map<String, String>> mdcAfter = new AtomicReference<>();
    AtomicReference<SecurityContext> securityAfter = new AtomicReference<>();

    runOnNewThread(() -> {
      try {
        decorated.run();
      } catch (IllegalStateException e) {
        thrown.set(e);
      }
      mdcAfter.set(MDC.getCopyOfContextMap());
      securityAfter.set(SecurityContextHolder.getContext());
    });

    assertSoftly(softly -> {
      softly.assertThat(thrown.get()).hasMessage("boom");
      softly.assertThat(mdcAfter.get()).isNullOrEmpty();
      softly.assertThat(securityAfter.get().getAuthentication()).isNull();
    });
  }

  @Test
  void should_runTaskWithEmptyMdc_when_callerHasNoMdc() throws Exception {
    AtomicReference<Map<String, String>> mdcSeen = new AtomicReference<>();
    AtomicReference<Boolean> ran = new AtomicReference<>(false);
    Runnable decorated =
        decorator.decorate(() -> {
          mdcSeen.set(MDC.getCopyOfContextMap());
          ran.set(true);
        });

    runOnNewThread(decorated);

    assertSoftly(softly -> {
      softly.assertThat(ran.get()).isTrue();
      softly.assertThat(mdcSeen.get()).isNullOrEmpty();
    });
  }

  private static void runOnNewThread(Runnable runnable) throws InterruptedException {
    Thread thread = Thread.ofVirtual().start(runnable);
    thread.join();
  }
}
