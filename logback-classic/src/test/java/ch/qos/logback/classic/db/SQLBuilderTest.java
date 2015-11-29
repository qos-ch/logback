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
package ch.qos.logback.classic.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ch.qos.logback.classic.db.names.DBNameResolver;
import ch.qos.logback.classic.db.names.DefaultDBNameResolver;
import ch.qos.logback.classic.db.names.SimpleDBNameResolver;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-22
 */
public class SQLBuilderTest {

    @Test
    public void shouldReturnDefaultSqlInsertLoggingEventQuery() throws Exception {
        // given
        DBNameResolver nameResolver = new DefaultDBNameResolver();

        // when
        String sql = SQLBuilder.buildInsertSQL(nameResolver);

        // then
        final String expected = "INSERT INTO logging_event (timestmp, formatted_message, logger_name, level_string, thread_name, reference_flag, arg0, arg1, arg2, arg3, caller_filename, caller_class, caller_method, caller_line) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    public void shouldReturnDefaultSqlInsertExceptionQuery() throws Exception {
        // given
        DBNameResolver nameResolver = new DefaultDBNameResolver();

        // when
        String sql = SQLBuilder.buildInsertExceptionSQL(nameResolver);

        // then
        final String expected = "INSERT INTO logging_event_exception (event_id, i, trace_line) VALUES (?, ?, ?)";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    public void shouldReturnDefaultSqlInsertLoggingPropertyQuery() throws Exception {
        // given
        DBNameResolver nameResolver = new DefaultDBNameResolver();

        // when
        String sql = SQLBuilder.buildInsertPropertiesSQL(nameResolver);

        // then
        final String expected = "INSERT INTO logging_event_property (event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";
        assertThat(sql).isEqualTo(expected);
    }

    private DBNameResolver createSimpleDBNameResolver() {
        final SimpleDBNameResolver nameResolver = new SimpleDBNameResolver();
        nameResolver.setTableNamePrefix("tp_");
        nameResolver.setTableNameSuffix("_ts");
        nameResolver.setColumnNamePrefix("cp_");
        nameResolver.setColumnNameSuffix("_cs");
        return nameResolver;
    }

    @Test
    public void shouldReturnSimpleSqlInsertLoggingEventQuery() throws Exception {
        // given
        DBNameResolver nameResolver = createSimpleDBNameResolver();

        // when
        String sql = SQLBuilder.buildInsertSQL(nameResolver);

        // then
        final String expected = "INSERT INTO tp_logging_event_ts (cp_timestmp_cs, cp_formatted_message_cs, cp_logger_name_cs, cp_level_string_cs, cp_thread_name_cs, cp_reference_flag_cs, cp_arg0_cs, cp_arg1_cs, cp_arg2_cs, cp_arg3_cs, cp_caller_filename_cs, cp_caller_class_cs, cp_caller_method_cs, cp_caller_line_cs) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    public void shouldReturnSimpleSqlInsertExceptionQuery() throws Exception {
        // given
        DBNameResolver nameResolver = createSimpleDBNameResolver();

        // when
        String sql = SQLBuilder.buildInsertExceptionSQL(nameResolver);

        // then
        final String expected = "INSERT INTO tp_logging_event_exception_ts (cp_event_id_cs, cp_i_cs, cp_trace_line_cs) VALUES (?, ?, ?)";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    public void shouldReturnSimpleSqlInsertLoggingPropertyQuery() throws Exception {
        // given
        DBNameResolver nameResolver = createSimpleDBNameResolver();

        // when
        String sql = SQLBuilder.buildInsertPropertiesSQL(nameResolver);

        // then
        final String expected = "INSERT INTO tp_logging_event_property_ts (cp_event_id_cs, cp_mapped_key_cs, cp_mapped_value_cs) VALUES (?, ?, ?)";
        assertThat(sql).isEqualTo(expected);
    }

}
