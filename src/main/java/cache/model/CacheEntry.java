package cache.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class CacheEntry<V> {
  private final V value;
  private final Instant createdAt;
  private final Duration ttl;
  private final Clock clock;

  public CacheEntry(V value, Instant createdAt, Duration ttl, Clock clock) {
    this.value = value;
    this.createdAt = createdAt;
    this.ttl = ttl;
    this.clock = clock;
  }

  // public CacheEntry(V value, Duration ttl) {
  // this(value, Instant.now(Clock.systemUTC()), ttl, Clock.systemUTC());
  // }

  public boolean isExpired() {
    return clock.instant().isAfter(createdAt.plus(ttl));
  }

  public V getValue() {
    return value;
  }
}
