package ch.qos.logback.classic.db.names;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-18
 */
public class CustomDBNameResolverTest {
  private CustomDBNameResolver resolver;

  @Before
  public void setUp() throws Exception {
    resolver = new CustomDBNameResolver();
  }

  @Test
  public void shouldReturnDefaultTableName() throws Exception {
    //when
    String tableName = resolver.getTableName(TableName.LOGGING_EVENT);

    //then
    assertThat(tableName).isEqualTo("logging_event");
  }

  @Test
  public void shouldReturnDefaultLoggingEventColumnName() throws Exception {
    //when
    String columnName = resolver.getLoggingEventColumnName(LoggingEventColumnName.LOGGER_NAME);

    //then
    assertThat(columnName).isEqualTo("logger_name");
  }

  @Test
  public void shouldReturnDefaultLoggingPropertyColumnName() throws Exception {
    //when
    String columnName = resolver.getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName.MAPPED_VALUE);

    //then
    assertThat(columnName).isEqualTo("mapped_value");
  }

  @Test
  public void shouldReturnDefaultLoggingExceptionColumnName() throws Exception {
    //when
    String columnName = resolver.getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName.EVENT_ID);

    //then
    assertThat(columnName).isEqualTo("event_id");
  }

  @Test
  public void shouldReturnModifiedTableName() throws Exception {
    //when
    resolver.setLoggingEventExceptionTableName("tbl_logging_event_exception");
    String tableName = resolver.getTableName(TableName.LOGGING_EVENT_EXCEPTION);

    //then
    assertThat(tableName).isEqualTo("tbl_logging_event_exception");
  }

  @Test
  public void shouldReturnModifiedLoggingEventColumnName() throws Exception {
    //when
    resolver.setLoggingEventCallerFilenameColumnName("c_caller_filename");
    String columnName = resolver.getLoggingEventColumnName(LoggingEventColumnName.CALLER_FILENAME);

    //then
    assertThat(columnName).isEqualTo("c_caller_filename");
  }

  @Test
  public void shouldReturnModifiedLoggingPropertyColumnName() throws Exception {
    //when
    resolver.setLoggingEventPropertyMappedKeyColumnName("c_mapped_key");
    String columnName = resolver.getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName.MAPPED_KEY);

    //then
    assertThat(columnName).isEqualTo("c_mapped_key");
  }

  @Test
  public void shouldReturnModifiedLoggingExceptionColumnName() throws Exception {
    //when
    resolver.setLoggingEventExceptionIColumnName("c_i");
    String columnName = resolver.getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName.I);

    //then
    assertThat(columnName).isEqualTo("c_i");
  }

  @Test
  public void shouldReturnDefaultTableNameWhenNullGiven() throws Exception {
    //when
    resolver.setLoggingEventExceptionTableName(null);
    String tableName = resolver.getTableName(TableName.LOGGING_EVENT_PROPERTY);

    //then
    assertThat(tableName).isEqualTo("logging_event_property");
  }

  @Test
  public void shouldReturnDefaultLoggingEventColumnNameWhenNullGiven() throws Exception {
    //when
    resolver.setLoggingEventCallerFilenameColumnName(null);
    String columnName = resolver.getLoggingEventColumnName(LoggingEventColumnName.CALLER_CLASS);

    //then
    assertThat(columnName).isEqualTo("caller_class");
  }

  @Test
  public void shouldReturnDefaultLoggingPropertyColumnNameWhenNullGiven() throws Exception {
    //when
    resolver.setLoggingEventPropertyMappedKeyColumnName(null);
    String columnName = resolver.getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName.EVENT_ID);

    //then
    assertThat(columnName).isEqualTo("event_id");
  }

  @Test
  public void shouldReturnDefaultLoggingExceptionColumnNameWhenNullGiven() throws Exception {
    //when
    resolver.setLoggingEventExceptionIColumnName(null);
    String columnName = resolver.getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName.I);

    //then
    assertThat(columnName).isEqualTo("i");
  }

}
