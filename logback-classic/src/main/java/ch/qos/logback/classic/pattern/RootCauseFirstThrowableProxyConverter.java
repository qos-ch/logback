/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

/**
 * @author Tomasz Nurkiewicz
 * @since 0.9.30
 */
public class RootCauseFirstThrowableProxyConverter extends ExtendedThrowableProxyConverter {

  @Override
  protected String throwableProxyToString(IThrowableProxy tp) {
    StringBuilder buf = new StringBuilder(BUILDER_CAPACITY);
    recursiveAppendRootCauseFirst(buf, null, ThrowableProxyUtil.REGULAR_EXCEPTION_INDENT, tp);
    return buf.toString();
  }

  protected void recursiveAppendRootCauseFirst(StringBuilder sb, String prefix, int indent, IThrowableProxy tp) {
    if (tp.getCause() != null) {
      recursiveAppendRootCauseFirst(sb, prefix, indent, tp.getCause());
      prefix = null; // to avoid adding it more than once
    }
    ThrowableProxyUtil.indent(sb, indent - 1);
    if (prefix != null) {
      sb.append(prefix);
    }
    ThrowableProxyUtil.subjoinFirstLineRootCauseFirst(sb, tp);
    sb.append(CoreConstants.LINE_SEPARATOR);
    subjoinSTEPArray(sb, indent, tp);
    IThrowableProxy[] suppressed = tp.getSuppressed();
    if(suppressed != null) {
      for(IThrowableProxy current : suppressed) {
        recursiveAppendRootCauseFirst(sb, CoreConstants.SUPPRESSED, indent + ThrowableProxyUtil.SUPPRESSED_EXCEPTION_INDENT, current);
      }
    }
  }
}
