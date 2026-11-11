package it.datafinance.scrape_engine.infrastructure.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface AsyncService {

  <R> CompletableFuture<R> execute(Supplier<R> supplier);
}
