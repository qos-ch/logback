/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

public class DuplicateMessageFilter extends TurboFilter {

  static final int DEFAULT_CACHE_SIZE = 100;
  static final int DEFAULT_ALLOWED_REPETITIONS = 5;
 
  public int allowedRepetitions = DEFAULT_ALLOWED_REPETITIONS;
  public int cacheSize = DEFAULT_CACHE_SIZE;
  
  private LRUMessageCache msgCache;
  
  @Override
  public void start() {
    msgCache = new LRUMessageCache(cacheSize);
    super.start();
  }
 
  @Override
  public void stop() {
    msgCache.clear();
    msgCache = null;
    super.stop();
  }

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level,
      String format, Object[] params, Throwable t) {
    int count = msgCache.getMessageCount(format);
    if(count <= allowedRepetitions) {
      return FilterReply.NEUTRAL;
    } else {
      return FilterReply.DENY;
    }
  }

  public int getAllowedRepetitions() {
    return allowedRepetitions;
  }

  public void setAllowedRepetitions(int allowedRepetitions) {
    this.allowedRepetitions = allowedRepetitions;
  }

  public int getCacheSize() {
    return cacheSize;
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }
  
}
