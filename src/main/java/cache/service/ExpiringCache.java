package cache.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import cache.Cache;
import cache.model.CacheEntry;

public class ExpiringCache<K, V> implements Cache<K, V> {
  ConcurrentHashMap<K, CacheEntry<V>> storage;
  Duration defaultTtl;
  int maxSize;
  private final Clock clock;
  private final ScheduledExecutorService cleanupExecutor;

  public ExpiringCache(Duration ttl, int maxSize) {
    this(ttl, maxSize, Clock.systemUTC(), Duration.ofSeconds(5));
  }

  ExpiringCache(Duration ttl, int maxSize, Duration cleanupInterval) {
    this(ttl, maxSize, Clock.systemUTC(), cleanupInterval);
  }

  ExpiringCache(Duration ttl, int maxSize, Clock clock, Duration cleanupInterval) {
    if (ttl == null || ttl.isNegative() || maxSize <= 0) {
      throw new IllegalArgumentException("Invalid TTL or maxSize");
    }
    this.defaultTtl = ttl;
    this.maxSize = maxSize;
    this.clock = clock;
    this.storage = new ConcurrentHashMap<>();

    this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 0,
        cleanupInterval.toMillis(), TimeUnit.MILLISECONDS);
  }

  public void evictOneEntry() {
    storage.keySet().stream().findFirst().ifPresent(storage::remove);
  }

  private void cleanupExpiredEntries() {
    try {
      storage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    } catch (Exception e) {
      System.err.println("Cache cleanup failed: " + e.getMessage());
    }
  }

  @Override
  public void put(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("Key and value must not be nulls");
    }
    if (storage.containsKey(key)) {
      Instant now = clock.instant();
      storage.put(key, new CacheEntry<>(value, now, defaultTtl, clock));
      return;
    }
    if (storage.size() >= maxSize) {
      evictOneEntry();
    }

    Instant now = clock.instant();
    storage.put(key, new CacheEntry<>(value, now, defaultTtl, clock));
  };

  @Override
  public V get(K key) {
    if (key == null) {
      return null;
    }
    CacheEntry<V> entry = storage.get(key);
    if (entry == null) {
      return null;
    }
    if (entry.isExpired()) {
      storage.remove(key);
      return null;
    }
    return entry.getValue();
  }

  @Override
  public void remove(K key) {
    storage.remove(key);
  };

  @Override
  public void clear() {
    storage.clear();
  };

  @Override
  public int size() {
    return storage.size();
  };

  @Override
  public void shutdown() {
    cleanupExecutor.shutdown();
    try {
      if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        cleanupExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      cleanupExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  };
}
