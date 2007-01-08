/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;

public interface Layout<E> extends ContextAware, LifeCycle {
  // Note that the line.separator property can be looked up even by applets.
  public static final String LINE_SEP = System.getProperty("line.separator");
  public static final int LINE_SEP_LEN = LINE_SEP.length();
  
  /**
   * Transform an event (of type Object) and return it as a String after 
   * appropriate formatting.
   * 
   * <p>Taking in an object and returning a String is the least sophisticated
   * way of formatting events. However, it is remarkably CPU-effective.
   * </p>
   * 
   * @param event The event to format
   * @return the event formatted as a String
   */
  String doLayout(E event);
  
  /**
   * Return the file header for this layout. The returned value may be null.
   * @return The header.
   */
  String getFileHeader();

  /**
   * Return the header of the logging event formatting. The returned value
   * may be null.
   * 
   * @return The header.
   */
  String getPresentationHeader();

  /**
   * Return the footer of the logging event formatting. The returned value
   * may be null.
   * 
   * @return The footer.
   */
  
  String getPresentationFooter();
  
  /**
   * Return the file footer for this layout. The returned value may be null.
   * @return The footer.
   */
  String getFileFooter();
  
  /**
   * Returns the content type as appropriate for the implementation.
   *  
   * @return
   */
  String getContentType();
  
}
