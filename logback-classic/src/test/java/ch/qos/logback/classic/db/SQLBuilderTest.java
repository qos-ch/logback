package ch.qos.logback.classic.db;

import ch.qos.logback.classic.db.names.*;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-22
 */
public class SQLBuilderTest {

  @Test
  public void shouldReturnDefaultSqlInsertLoggingEventQuery() throws Exception {
    //given
    DBNameResolver nameResolver = new DefaultDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertSQL(nameResolver);

    //then
    final String expected = "INSERT INTO logging_event (timestmp, formatted_message, logger_name, level_string, thread_name, reference_flag, arg0, arg1, arg2, arg3, caller_filename, caller_class, caller_method, caller_line) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  @Test
  public void shouldReturnDefaultSqlInsertExceptionQuery() throws Exception {
    //given
    DBNameResolver nameResolver = new DefaultDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertExceptionSQL(nameResolver);

    //then
    final String expected = "INSERT INTO logging_event_exception (event_id, i, trace_line) VALUES (?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  @Test
  public void shouldReturnDefaultSqlInsertLoggingPropertyQuery() throws Exception {
    //given
    DBNameResolver nameResolver = new DefaultDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertPropertiesSQL(nameResolver);

    //then
    final String expected = "INSERT INTO logging_event_property (event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  @Test
  public void shouldReturnCustomSqlInsertLoggingEventQuery() throws Exception {
    //given
    DBNameResolver nameResolver = createCustomDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertSQL(nameResolver);

    //then
    final String expected = "INSERT INTO alpha (a, b, c, d, e, f, a0, a1, a2, a3, g, h, i, j) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  private DBNameResolver createCustomDBNameResolver() {
    final CustomDBNameResolver nameResolver = new CustomDBNameResolver();
    nameResolver.overrideTableName(TableName.LOGGING_EVENT, "alpha");
    nameResolver.overrideTableName(TableName.LOGGING_EVENT_EXCEPTION.name(), "beta");
    nameResolver.overrideTableName(TableName.LOGGING_EVENT_PROPERTY, "gamma");

    nameResolver.overrideColumnName(ColumnName.TIMESTMP, "a");
    nameResolver.overrideColumnName(ColumnName.FORMATTED_MESSAGE, "b");
    nameResolver.overrideColumnName(ColumnName.LOGGER_NAME, "c");
    nameResolver.overrideColumnName(ColumnName.LEVEL_STRING, "d");
    nameResolver.overrideColumnName(ColumnName.THREAD_NAME, "e");
    nameResolver.overrideColumnName(ColumnName.REFERENCE_FLAG, "f");
    nameResolver.overrideColumnName(ColumnName.ARG0.name(), "a0");
    nameResolver.overrideColumnName(ColumnName.ARG1.name(), "a1");
    nameResolver.overrideColumnName(ColumnName.ARG2.name(), "a2");
    nameResolver.overrideColumnName(ColumnName.ARG3.name(), "a3");
    nameResolver.overrideColumnName(ColumnName.CALLER_FILENAME.name(), "g");
    nameResolver.overrideColumnName(ColumnName.CALLER_CLASS.name(), "h");
    nameResolver.overrideColumnName(ColumnName.CALLER_METHOD.name(), "i");
    nameResolver.overrideColumnName(ColumnName.CALLER_LINE.name(), "j");

    nameResolver.overrideColumnName(ColumnName.EVENT_ID.name(), "k");
    nameResolver.overrideColumnName(ColumnName.I.name(), "l");
    nameResolver.overrideColumnName(ColumnName.TRACE_LINE.name(), "m");

    nameResolver.overrideColumnName(ColumnName.MAPPED_KEY.name(), "o");
    nameResolver.overrideColumnName(ColumnName.MAPPED_VALUE.name(), "p");
    return nameResolver;
  }

  @Test
  public void shouldReturnCustomSqlInsertExceptionQuery() throws Exception {
    //given
    DBNameResolver nameResolver = createCustomDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertExceptionSQL(nameResolver);

    //then
    final String expected = "INSERT INTO beta (k, l, m) VALUES (?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  @Test
  public void shouldReturnCustomSqlInsertLoggingPropertyQuery() throws Exception {
    //given
    DBNameResolver nameResolver = createCustomDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertPropertiesSQL(nameResolver);

    //then
    final String expected = "INSERT INTO gamma (k, o, p) VALUES (?, ?, ?)";
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
    //given
    DBNameResolver nameResolver = createSimpleDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertSQL(nameResolver);

    //then
    final String expected = "INSERT INTO tp_logging_event_ts (cp_timestmp_cs, cp_formatted_message_cs, cp_logger_name_cs, cp_level_string_cs, cp_thread_name_cs, cp_reference_flag_cs, cp_arg0_cs, cp_arg1_cs, cp_arg2_cs, cp_arg3_cs, cp_caller_filename_cs, cp_caller_class_cs, cp_caller_method_cs, cp_caller_line_cs) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  @Test
  public void shouldReturnSimpleSqlInsertExceptionQuery() throws Exception {
    //given
    DBNameResolver nameResolver = createSimpleDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertExceptionSQL(nameResolver);

    //then
    final String expected = "INSERT INTO tp_logging_event_exception_ts (cp_event_id_cs, cp_i_cs, cp_trace_line_cs) VALUES (?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  @Test
  public void shouldReturnSimpleSqlInsertLoggingPropertyQuery() throws Exception {
    //given
    DBNameResolver nameResolver = createSimpleDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertPropertiesSQL(nameResolver);

    //then
    final String expected = "INSERT INTO tp_logging_event_property_ts (cp_event_id_cs, cp_mapped_key_cs, cp_mapped_value_cs) VALUES (?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

}
