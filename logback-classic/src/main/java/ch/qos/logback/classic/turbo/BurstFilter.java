/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2017, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Marker;

/**
 * The <code>BurstFilter</code> is a logging filter that regulates logging traffic.
 *
 * <p>
 * Use this filter when you want to control the maximum burst of log statements that can be sent to an appender. The
 * filter is configured in the log4j configuration file. For example, the following configuration limits the number of
 * INFO level (as well as DEBUG and TRACE) log statements that can be sent to the console to a burst of 100 with an
 * average rate of 16 per second. WARN, ERROR and FATAL messages would continue to be delivered.
 * </p>
 *
 * @author tshang.
 */
public class BurstFilter extends TurboFilter {


  private static final long NANOS_IN_SECONDS = 1000000000;

  private static final int DEFAULT_RATE = 10;

  private static final int DEFAULT_RATE_MULTIPLE = 100;

  private static final int HASH_SHIFT = 32;

  /**
   * Levels of messages to be filtered. Multiple level elements can be defined.
   */
  private Set<Level> levels = new HashSet<>();

  private float rate;

  private long maxBurst;

  private long burstInterval;

  private final DelayQueue<LogDelay> history = new DelayQueue<>();

  private final Queue<LogDelay> available = new ConcurrentLinkedQueue<>();

  static LogDelay createLogDelay(final long expireTime) {
    return new LogDelay(expireTime);
  }

  public void setLevel(Level level) {
    this.levels.add(level);
  }

  public void setRate(float rate) {
    this.rate = rate;
  }

  public void setMaxBurst(long maxBurst) {
    this.maxBurst = maxBurst;
  }

  @Override
  public void start() {
    if (this.rate <= 0) {
      this.rate = DEFAULT_RATE;
    }
    if (this.maxBurst <= 0) {
      this.maxBurst = (long) (this.rate * DEFAULT_RATE_MULTIPLE);
    }
    this.burstInterval = (long) (NANOS_IN_SECONDS * (maxBurst / rate));
    for (int i = 0; i < maxBurst; ++i) {
      available.add(createLogDelay(0));
    }
    super.start();
  }

  @Override
  public void stop() {
    this.rate = 0f;
    this.maxBurst = 0;
    history.clear();
    available.clear();
    super.stop();
  }

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects,
      Throwable throwable) {
    if (this.levels.contains(level)) {
      LogDelay delay = history.poll();
      while (delay != null) {
        available.add(delay);
        delay = history.poll();
      }
      delay = available.poll();
      if (delay != null) {
        delay.setDelay(burstInterval);
        history.add(delay);
        return FilterReply.NEUTRAL;
      }
      return FilterReply.DENY;
    }
    return FilterReply.NEUTRAL;
  }

  /**
   * Delay object to represent each log event that has occurred within the timespan.
   *
   * Consider this class private, package visibility for testing.
   */
  private static class LogDelay implements Delayed {

    LogDelay(final long expireTime) {
      this.expireTime = expireTime;
    }

    private long expireTime;

    public void setDelay(final long delay) {
      this.expireTime = delay + System.nanoTime();
    }

    @Override
    public long getDelay(final TimeUnit timeUnit) {
      return timeUnit.convert(expireTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(final Delayed delayed) {
      final long diff = this.expireTime - ((LogDelay) delayed).expireTime;
      return Long.signum(diff);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final LogDelay logDelay = (LogDelay) o;

      if (expireTime != logDelay.expireTime) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return (int) (expireTime ^ (expireTime >>> HASH_SHIFT));
    }
  }
}
