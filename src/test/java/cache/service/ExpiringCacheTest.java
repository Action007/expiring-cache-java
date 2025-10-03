package cache.service;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ExpiringCacheTest {

  private ExpiringCache<String, String> cache;

  @AfterEach
  void cleanup() {
    if (cache != null) {
      cache.shutdown();
    }
  }

  @Test
  void entryExpiresAfterTTL() throws InterruptedException {
    cache = new ExpiringCache<>(Duration.ofMillis(100), 10, Duration.ofSeconds(1));
    cache.put("key", "value");
    assertEquals("value", cache.get("key"));

    Thread.sleep(150);

    assertNull(cache.get("key"));
  }

  @Test
  void cleanupRemovesExpiredEntries() throws InterruptedException {
    cache = new ExpiringCache<>(Duration.ofMillis(100), 10, Duration.ofMillis(200));
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    assertEquals(2, cache.size());

    Thread.sleep(150);
    assertEquals(2, cache.size());

    Thread.sleep(100);
    assertEquals(0, cache.size());
  }
}
