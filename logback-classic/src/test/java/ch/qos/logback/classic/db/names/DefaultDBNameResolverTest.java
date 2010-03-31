package ch.qos.logback.classic.db.names;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

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
    //when
    String columnName = resolver.getColumnName(ColumnName.LOGGER_NAME);

    //then
    assertThat(columnName).isEqualTo("logger_name");
  }

  @Test
  public void testGetLoggingEventPropertyColumnName() throws Exception {
    //when
    String columnName = resolver.getColumnName(ColumnName.MAPPED_KEY);

    //then
    assertThat(columnName).isEqualTo("mapped_key");
  }

  @Test
  public void testGetLoggingEventExceptionColumnName() throws Exception {
    //when
    String columnName = resolver.getColumnName(ColumnName.TRACE_LINE);

    //then
    assertThat(columnName).isEqualTo("trace_line");
  }

  @Test
  public void testGetTableName() throws Exception {
    //when
    String tableName = resolver.getTableName(TableName.LOGGING_EVENT_EXCEPTION);

    //then
    assertThat(tableName).isEqualTo("logging_event_exception");
  }

}
