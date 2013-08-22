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
package ch.qos.logback.classic;


/**
 * fully qualified class name
 * @author wuwen.55@gmail.com
 */
public final class FQCNHelper {

	/**
	  * The fully qualified name of this class. Used in gathering caller
	  * information.
	  */
	public static final String FQCN = ch.qos.logback.classic.Logger.class
	      .getName();

	/**
	 * in order to support custom  caller information.
	 */
	static ThreadLocal<String> FQCNLOCAL = new ThreadLocal<String>();

	private FQCNHelper() {
	}

	public static String getFQCN() {
		String fqcnStr = FQCNLOCAL.get();
		return fqcnStr == null || "".equals(fqcnStr) ? FQCN : fqcnStr;
	}

	public static void setFQCN(String fqcnStr) {
		FQCNLOCAL.set(fqcnStr);
	}

	public static void clear() {
		FQCNLOCAL.remove();
	}

}
