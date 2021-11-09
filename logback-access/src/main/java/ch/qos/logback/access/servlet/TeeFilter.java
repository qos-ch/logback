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
package ch.qos.logback.access.servlet;

import static ch.qos.logback.access.AccessConstants.LB_OUTPUT_BUFFER;
import static ch.qos.logback.access.AccessConstants.TEE_FILTER_EXCLUDES_PARAM;
import static ch.qos.logback.access.AccessConstants.TEE_FILTER_INCLUDES_PARAM;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TeeFilter implements Filter {

	boolean active;

	@Override
	public void destroy() {
		// NOP
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {

		if (active && request instanceof HttpServletRequest) {
			try {
				final TeeHttpServletRequest teeRequest = new TeeHttpServletRequest((HttpServletRequest) request);
				final TeeHttpServletResponse teeResponse = new TeeHttpServletResponse((HttpServletResponse) response);

				// System.out.println("BEFORE TeeFilter. filterChain.doFilter()");
				filterChain.doFilter(teeRequest, teeResponse);
				// System.out.println("AFTER TeeFilter. filterChain.doFilter()");

				teeResponse.finish();
				// let the output contents be available for later use by
				// logback-access-logging
				teeRequest.setAttribute(LB_OUTPUT_BUFFER, teeResponse.getOutputBuffer());
			} catch (final IOException | ServletException e) {
				e.printStackTrace();
				throw e;
			}
		} else {
			filterChain.doFilter(request, response);
		}

	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		final String includeListAsStr = filterConfig.getInitParameter(TEE_FILTER_INCLUDES_PARAM);
		final String excludeListAsStr = filterConfig.getInitParameter(TEE_FILTER_EXCLUDES_PARAM);
		final String localhostName = getLocalhostName();

		active = computeActivation(localhostName, includeListAsStr, excludeListAsStr);
		if (active) {
			System.out.println("TeeFilter will be ACTIVE on this host [" + localhostName + "]");
		} else {
			System.out.println("TeeFilter will be DISABLED on this host [" + localhostName + "]");
		}

	}

	static List<String> extractNameList(String nameListAsStr) {
		final List<String> nameList = new ArrayList<>();
		if (nameListAsStr == null) {
			return nameList;
		}

		nameListAsStr = nameListAsStr.trim();
		if (nameListAsStr.length() == 0) {
			return nameList;
		}

		final String[] nameArray = nameListAsStr.split("[,;]");
		for (String n : nameArray) {
			n = n.trim();
			nameList.add(n);
		}
		return nameList;
	}

	static String getLocalhostName() {
		String hostname = "127.0.0.1";

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException uhe) {
			uhe.printStackTrace();
		}
		return hostname;
	}

	static boolean computeActivation(final String hostname, final String includeListAsStr, final String excludeListAsStr) {
		final List<String> includeList = extractNameList(includeListAsStr);
		final List<String> excludeList = extractNameList(excludeListAsStr);
		final boolean inIncludesList = mathesIncludesList(hostname, includeList);
		final boolean inExcludesList = mathesExcludesList(hostname, excludeList);
		return inIncludesList && !inExcludesList;
	}

	static boolean mathesIncludesList(final String hostname, final List<String> includeList) {
		if (includeList.isEmpty()) {
			return true;
		}
		return includeList.contains(hostname);
	}

	static boolean mathesExcludesList(final String hostname, final List<String> excludesList) {
		if (excludesList.isEmpty()) {
			return false;
		}
		return excludesList.contains(hostname);
	}

}
