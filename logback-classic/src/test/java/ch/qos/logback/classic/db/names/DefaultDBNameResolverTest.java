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
package ch.qos.logback.classic.db.names;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-18
 */
public class DefaultDBNameResolverTest {

    private DefaultDBNameResolver resolver;

    @Before
    public void setUp() throws Exception {
        resolver = new DefaultDBNameResolver();
    }

    @Test
    public void testGetLoggingEventColumnName() throws Exception {
        // when
        String columnName = resolver.getColumnName(ColumnName.LOGGER_NAME);

        // then
        assertThat(columnName).isEqualTo("logger_name");
    }

    @Test
    public void testGetLoggingEventPropertyColumnName() throws Exception {
        // when
        String columnName = resolver.getColumnName(ColumnName.MAPPED_KEY);

        // then
        assertThat(columnName).isEqualTo("mapped_key");
    }

    @Test
    public void testGetLoggingEventExceptionColumnName() throws Exception {
        // when
        String columnName = resolver.getColumnName(ColumnName.TRACE_LINE);

        // then
        assertThat(columnName).isEqualTo("trace_line");
    }

    @Test
    public void testGetTableName() throws Exception {
        // when
        String tableName = resolver.getTableName(TableName.LOGGING_EVENT_EXCEPTION);

        // then
        assertThat(tableName).isEqualTo("logging_event_exception");
    }

}
