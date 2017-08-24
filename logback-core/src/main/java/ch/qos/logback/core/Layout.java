/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core;

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;

public interface Layout<E> extends ContextAware, LifeCycle {

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
