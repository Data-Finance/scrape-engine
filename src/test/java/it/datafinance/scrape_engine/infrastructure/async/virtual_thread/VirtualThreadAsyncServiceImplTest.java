package it.datafinance.scrape_engine.infrastructure.async.virtual_thread;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

class VirtualThreadAsyncServiceImplTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final SimpleAsyncTaskExecutor executor =
      new SimpleAsyncTaskExecutorBuilder()
          .virtualThreads(true)
          .threadNamePrefix("vt-")
          .taskDecorator(new ContextCopyingTaskDecorator())
          .build();
  private final VirtualThreadAsyncServiceImpl service = new VirtualThreadAsyncServiceImpl(executor);

  @AfterEach
  void clearCallerContext() {
    MDC.clear();
  }

  @Test
  void should_returnSupplierResult_when_executed() {
    CompletableFuture<String> future = service.execute(() -> "result");

    assertSoftly(softly -> softly.assertThat(future).succeedsWithin(TIMEOUT).isEqualTo("result"));
  }

  @Test
  void should_runOnNamedVirtualThread_when_executed() {
    CompletableFuture<Thread> future = service.execute(Thread::currentThread);

    assertSoftly(softly -> {
      Thread thread = future.join();
      softly.assertThat(thread.isVirtual()).isTrue();
      softly.assertThat(thread.getName()).startsWith("vt-");
      softly.assertThat(thread).isNotSameAs(Thread.currentThread());
    });
  }

  @Test
  void should_completeExceptionally_when_supplierThrows() {
    CompletableFuture<Object> future =
        service.execute(() -> {
          throw new IllegalStateException("boom");
        });

    assertSoftly(softly ->
        softly
            .assertThat(future)
            .failsWithin(TIMEOUT)
            .withThrowableThat()
            .havingRootCause()
            .isInstanceOf(IllegalStateException.class)
            .withMessage("boom"));
  }

  @Test
  void should_propagateCallerMdc_when_executed() {
    MDC.put("traceId", "abc-123");
    AtomicReference<String> traceIdSeen = new AtomicReference<>();

    CompletableFuture<Void> future =
        service.execute(() -> {
          traceIdSeen.set(MDC.get("traceId"));
          return null;
        });

    assertSoftly(softly -> {
      softly.assertThat(future).succeedsWithin(TIMEOUT);
      softly.assertThat(traceIdSeen.get()).isEqualTo("abc-123");
    });
  }
}
