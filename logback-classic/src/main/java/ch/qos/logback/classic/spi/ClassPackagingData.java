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
package ch.qos.logback.classic.spi;

import java.io.Serializable;
import java.util.Objects;

public class ClassPackagingData implements Serializable {

	private static final long serialVersionUID = -804643281218337001L;
	final String codeLocation;
	final String version;
	private final boolean exact;

	public ClassPackagingData(final String codeLocation, final String version) {
		this.codeLocation = codeLocation;
		this.version = version;
		exact = true;
	}

	public ClassPackagingData(final String classLocation, final String version, final boolean exact) {
		codeLocation = classLocation;
		this.version = version;
		this.exact = exact;
	}

	public String getCodeLocation() {
		return codeLocation;
	}

	public String getVersion() {
		return version;
	}

	public boolean isExact() {
		return exact;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		final int result = 1;
		return PRIME * result + (codeLocation == null ? 0 : codeLocation.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final ClassPackagingData other = (ClassPackagingData) obj;
		if (!Objects.equals(codeLocation, other.codeLocation) || (exact != other.exact) || !Objects.equals(version, other.version)) {
			return false;
		}
		return true;
	}

}
