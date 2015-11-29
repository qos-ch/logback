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
 * @since 2010-03-22
 */
public class SimpleDBNameResolverTest {

    private SimpleDBNameResolver nameResolver;

    @Before
    public void setUp() throws Exception {
        nameResolver = new SimpleDBNameResolver();
        /*
         * nameResolver.setTableNameSuffix("_ts"); nameResolver.setColumnNamePrefix("cp_");
         * nameResolver.setColumnNameSuffix("_cs");
         */
    }

    @Test
    public void shouldReturnTableNameWithPrefix() throws Exception {
        // given

        // when
        nameResolver.setTableNamePrefix("tp_");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("tp_logging_event");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("thread_name");
    }

    @Test
    public void shouldReturnTableNameWithSuffix() throws Exception {
        // given

        // when
        nameResolver.setTableNameSuffix("_ts");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("logging_event_ts");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("thread_name");
    }

    @Test
    public void shouldReturnTableNameWithBothPrefixAndSuffix() throws Exception {
        // given

        // when
        nameResolver.setTableNamePrefix("tp_");
        nameResolver.setTableNameSuffix("_ts");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("tp_logging_event_ts");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("thread_name");
    }

    @Test
    public void shouldReturnColumnNameWithPrefix() throws Exception {
        // given

        // when
        nameResolver.setColumnNamePrefix("cp_");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("logging_event");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("cp_thread_name");
    }

    @Test
    public void shouldReturnColumnNameWithSuffix() throws Exception {
        // given

        // when
        nameResolver.setColumnNameSuffix("_cs");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("logging_event");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("thread_name_cs");
    }

    @Test
    public void shouldReturnColumnNameWithBothPrefixAndSuffix() throws Exception {
        // given

        // when
        nameResolver.setColumnNamePrefix("cp_");
        nameResolver.setColumnNameSuffix("_cs");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("logging_event");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("cp_thread_name_cs");
    }

    @Test
    public void shouldReturnTableAndColumnNamesWithBothPrefixAndSuffix() throws Exception {
        // given

        // when
        nameResolver.setTableNamePrefix("tp_");
        nameResolver.setTableNameSuffix("_ts");
        nameResolver.setColumnNamePrefix("cp_");
        nameResolver.setColumnNameSuffix("_cs");

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("tp_logging_event_ts");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("cp_thread_name_cs");
    }

    @Test
    public void shouldHandleNullsAsEmptyStrings() throws Exception {
        // given

        // when
        nameResolver.setTableNamePrefix(null);
        nameResolver.setTableNameSuffix(null);
        nameResolver.setColumnNamePrefix(null);
        nameResolver.setColumnNameSuffix(null);

        // then
        assertThat(nameResolver.getTableName(TableName.LOGGING_EVENT)).isEqualTo("logging_event");
        assertThat(nameResolver.getColumnName(ColumnName.THREAD_NAME)).isEqualTo("thread_name");
    }

}
