/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.model;

public class InsertFromJNDIModel extends Model {

	private static final long serialVersionUID = -7803377963650426197L;
	
    public static final String ENV_ENTRY_NAME_ATTR = "env-entry-name";
    public static final String AS_ATTR = "as";
    
	String as;
	String envEntryName;
	String scopeStr;
	
	public String getScopeStr() {
		return scopeStr;
	}
	public void setScopeStr(String scopeStr) {
		this.scopeStr = scopeStr;
	}
	public String getAs() {
		return as;
	}
	public void setAs(String as) {
		this.as = as;
	}
	public String getEnvEntryName() {
		return envEntryName;
	}
	public void setEnvEntryName(String envEntryName) {
		this.envEntryName = envEntryName;
	}
	
	
	
}
