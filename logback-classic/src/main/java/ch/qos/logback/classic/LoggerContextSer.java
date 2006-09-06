/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import java.io.Serializable;
import java.util.Map;

/**
 * The object used to contain the LoggerContext informations once the
 * serialization has taken place.
 * 
 * @author S&eacute;bastien Pennec
 */
public class LoggerContextSer implements LoggerContextView, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6615558506849441711L;
	
	
	String name;
	Map<String, String> propertyMap;
	
	public String getName() {
		return name;
	}

	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

}
