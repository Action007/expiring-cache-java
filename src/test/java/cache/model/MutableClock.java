package cache.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

class MutableClock extends Clock {
  private Instant currentTime;
  private final ZoneId zone;

  public MutableClock(Instant initialTime) {
    this(initialTime, ZoneOffset.UTC);
  }

  public MutableClock(Instant initialTime, ZoneId zone) {
    this.currentTime = initialTime;
    this.zone = zone;
  }

  public void advance(Duration duration) {
    this.currentTime = currentTime.plus(duration);
  }

  public void setTime(Instant newTime) {
    this.currentTime = newTime;
  }

  @Override
  public Instant instant() {
    return currentTime;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    if (zone.equals(this.zone)) {
      return this;
    }
    return new MutableClock(currentTime, zone);
  }
}
