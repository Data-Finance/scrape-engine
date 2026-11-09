package it.datafinance.scrape_engine.utils;

import java.time.DateTimeException;
import java.time.ZoneId;
import org.jspecify.annotations.NonNull;

public class DateUtils {
  public static @NonNull ZoneId getZoneId(@NonNull String id) {
    try {
      return ZoneId.of(id);
    } catch (DateTimeException dateTimeException) {
      return ZoneId.of("Europe/Rome");
    }
  }
}
