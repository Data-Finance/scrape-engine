package it.datafinance.scrape_engine.infrastructure.async.virtual_thread.config;

import it.datafinance.scrape_engine.infrastructure.async.virtual_thread.ContextCopyingTaskDecorator;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
class VirtualThreadConfig {

  @Bean
  public SimpleAsyncTaskExecutor getVirtualThreadExecutor(SimpleAsyncTaskExecutorBuilder builder) {
    return builder.threadNamePrefix("vt-").taskDecorator(new ContextCopyingTaskDecorator()).build();
  }
}
