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
package ch.qos.logback.core.db;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.util.OptionHelper;

/**
 *
 * @author Ceki Gulcu
 *
 */
public class BindDataSourceToJNDIAction extends Action {

	static final String DATA_SOURCE_CLASS = "dataSourceClass";
	static final String URL = "url";
	static final String USER = "user";
	static final String PASSWORD = "password";
	private final BeanDescriptionCache beanDescriptionCache;

	public BindDataSourceToJNDIAction(final BeanDescriptionCache beanDescriptionCache) {
		this.beanDescriptionCache = beanDescriptionCache;
	}

	/**
	 * Instantiates an a data source and bind it to JNDI
	 * Most of the required parameters are placed in the ec.substitutionProperties
	 */
	@Override
	public void begin(final InterpretationContext ec, final String localName, final Attributes attributes) {
		final String dsClassName = ec.getProperty(DATA_SOURCE_CLASS);

		if (OptionHelper.isNullOrEmpty(dsClassName)) {
			addWarn("dsClassName is a required parameter");
			ec.addError("dsClassName is a required parameter");

			return;
		}

		final String urlStr = ec.getProperty(URL);
		final String userStr = ec.getProperty(USER);
		final String passwordStr = ec.getProperty(PASSWORD);

		try {
			final DataSource ds = (DataSource) OptionHelper.instantiateByClassName(dsClassName, DataSource.class, context);

			final PropertySetter setter = new PropertySetter(beanDescriptionCache,ds);
			setter.setContext(context);

			if (!OptionHelper.isNullOrEmpty(urlStr)) {
				setter.setProperty("url", urlStr);
			}

			if (!OptionHelper.isNullOrEmpty(userStr)) {
				setter.setProperty("user", userStr);
			}

			if (!OptionHelper.isNullOrEmpty(passwordStr)) {
				setter.setProperty("password", passwordStr);
			}

			final Context ctx = new InitialContext();
			ctx.rebind("dataSource", ds);
		} catch (final Exception oops) {
			addError("Could not bind  datasource. Reported error follows.", oops);
			ec.addError("Could not not bind  datasource of type [" + dsClassName + "].");
		}
	}

	@Override
	public void end(final InterpretationContext ec, final String name) {
	}
}
