package ch.qos.logback.classic.db;

import ch.qos.logback.classic.db.names.CustomDBNameResolver;
import ch.qos.logback.classic.db.names.DBNameResolver;
import ch.qos.logback.classic.db.names.DefaultDBNameResolver;
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
    final String expected = "INSERT INTO logging_event (timestmp, formatted_message, logger_name, level_string, thread_name, reference_flag, caller_filename, caller_class, caller_method, caller_line) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?)";
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
    final String expected = "INSERT INTO alpha (a, b, c, d, e, f, g, h, i, j) VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

  private DBNameResolver createCustomDBNameResolver() {
    final CustomDBNameResolver nameResolver = new CustomDBNameResolver();
    nameResolver.setLoggingEventTableName("alpha");
    nameResolver.setLoggingEventExceptionTableName("beta");
    nameResolver.setLoggingEventPropertyTableName("gamma");

    nameResolver.setLoggingEventTimestmpColumnName("a");
    nameResolver.setLoggingEventFormattedMessageColumnName("b");
    nameResolver.setLoggingEventLoggerNameColumnName("c");
    nameResolver.setLoggingEventLevelStringColumnName("d");
    nameResolver.setLoggingEventThreadNameColumnName("e");
    nameResolver.setLoggingEventReferenceFlagColumnName("f");
    nameResolver.setLoggingEventCallerFilenameColumnName("g");
    nameResolver.setLoggingEventCallerClassColumnName("h");
    nameResolver.setLoggingEventCallerMethodColumnName("i");
    nameResolver.setLoggingEventCallerLineColumnName("j");

    nameResolver.setLoggingEventExceptionEventIdColumnName("k");
    nameResolver.setLoggingEventExceptionIColumnName("l");
    nameResolver.setLoggingEventExceptionTraceLineColumnName("m");

    nameResolver.setLoggingEventPropertyEventIdColumnName("n");
    nameResolver.setLoggingEventPropertyMappedKeyColumnName("o");
    nameResolver.setLoggingEventPropertyMappedValueColumnName("p");
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
  public void shouldReturnCustomtSqlInsertLoggingPropertyQuery() throws Exception {
    //given
    DBNameResolver nameResolver = createCustomDBNameResolver();

    //when
    String sql = SQLBuilder.buildInsertPropertiesSQL(nameResolver);

    //then
    final String expected = "INSERT INTO gamma (n, o, p) VALUES (?, ?, ?)";
    assertThat(sql).isEqualTo(expected);
  }

}
