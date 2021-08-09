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
package ch.qos.logback.classic.pattern;

import static ch.qos.logback.core.CoreConstants.DOT;

public class TargetLengthBasedClassNameAbbreviator implements Abbreviator {

	final int targetLength;

	public TargetLengthBasedClassNameAbbreviator(int targetLength) {
		this.targetLength = targetLength;
	}

	
	public String abbreviate(String fqClassName) {
		if (fqClassName == null) {
			throw new IllegalArgumentException("Class name may not be null");
		}

		int inLen = fqClassName.length();
		if (inLen < targetLength) {
			return fqClassName;
		}

		StringBuilder buf = new StringBuilder(inLen);

		int rightMostDotIndex = fqClassName.lastIndexOf(DOT);

		if (rightMostDotIndex == -1)
			return fqClassName;

		// length of last segment including the dot
		int lastSegmentLength = inLen - rightMostDotIndex;

		int leftSegments_TargetLen = targetLength - lastSegmentLength;
		if (leftSegments_TargetLen < 0)
			leftSegments_TargetLen = 0;
		
		int leftSegmentsLen = inLen - lastSegmentLength;

		// maxPossibleTrim denotes the maximum number of characters we aim to trim
		// the actual number of character trimmed may be higher since segments, when
		// reduced, are reduced to just one character
		int maxPossibleTrim = leftSegmentsLen - leftSegments_TargetLen;

		int trimmed = 0;
		boolean inDotState = true;

		int i = 0;
		for (; i < rightMostDotIndex; i++) {
			char c = fqClassName.charAt(i);
			if (c == DOT) {
				// if trimmed too many characters, let us stop
				if (trimmed >= maxPossibleTrim)
					break;
				buf.append(c);
				inDotState = true;
			} else {
				if(inDotState) {
					buf.append(c);
					inDotState = false;
				} else {
					trimmed++;
				}
			}
		}
		// append from the position of i which may include the last seen DOT
		buf.append(fqClassName.substring(i));
		return buf.toString();
	}
}