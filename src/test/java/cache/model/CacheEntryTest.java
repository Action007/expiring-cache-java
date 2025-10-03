package cache.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import cache.model.CacheEntry;

public class CacheEntryTest {
  @Test
  void letsTestThisShit() {
    MutableClock testClock = new MutableClock(Instant.EPOCH);
    CacheEntry<String> entry =
        new CacheEntry<>("data", testClock.instant(), Duration.ofSeconds(5), testClock);

    assertFalse(entry.isExpired());

    testClock.advance(Duration.ofSeconds(10));

    assertTrue(entry.isExpired());
  }
}
