package it.datafinance.scrape_engine.infrastructure.async.virtual_thread;

import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ContextCopyingTaskDecorator implements TaskDecorator {

  @Override
  @NullUnmarked
  public Runnable decorate(@NonNull Runnable task) {

    Map<String, String> mdc = MDC.getCopyOfContextMap();
    SecurityContext security = SecurityContextHolder.getContext();

    return () -> {
      try {
        if (mdc != null) MDC.setContextMap(mdc);
        SecurityContextHolder.setContext(security);
        task.run();
      } finally {
        MDC.clear();
        SecurityContextHolder.clearContext();
      }
    };
  }
}
