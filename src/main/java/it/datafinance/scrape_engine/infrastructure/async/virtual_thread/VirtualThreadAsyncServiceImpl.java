package it.datafinance.scrape_engine.infrastructure.async.virtual_thread;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
class VirtualThreadAsyncServiceImpl implements VirtualThreadAsyncService {

  private final SimpleAsyncTaskExecutor executor;

  VirtualThreadAsyncServiceImpl(SimpleAsyncTaskExecutor executor) {
    this.executor = executor;
  }

  @Override
  public <R> CompletableFuture<R> execute(Supplier<R> supplier) {
    return CompletableFuture.supplyAsync(supplier, executor);
  }
}
