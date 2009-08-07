/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import java.util.Date;

/**
 * Given a date remove older archived log files.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface ArchiveRemover {
  public void clean(Date now);
  public void setMaxHistory(int maxHistory);
} 