package ch.qos.logback.classic.db.names;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-16
 */
public class CustomDBNameResolver implements DBNameResolver {

  private final DBNameResolver defaultDbNameResolver = new DefaultDBNameResolver();
  private final Map<TableName, String> tableNameOverrides = new EnumMap<TableName, String>(TableName.class);
  private final Map<LoggingEventColumnName, String> leColumnNameOverrides = new EnumMap<LoggingEventColumnName, String>(LoggingEventColumnName.class);
  private final Map<LoggingEventPropertyColumnName, String> lePropertyColumnNameOverrides = new EnumMap<LoggingEventPropertyColumnName, String>(LoggingEventPropertyColumnName.class);
  private final Map<LoggingEventExceptionColumnName, String> leExceptionColumnNameOverrides = new EnumMap<LoggingEventExceptionColumnName, String>(LoggingEventExceptionColumnName.class);

  public String getTableName(TableName tableName) {
    if (tableNameOverrides.get(tableName) != null)
      return tableNameOverrides.get(tableName);
    return defaultDbNameResolver.getTableName(tableName);
  }

  public String getLoggingEventColumnName(LoggingEventColumnName columnName) {
    if (leColumnNameOverrides.get(columnName) != null)
      return leColumnNameOverrides.get(columnName);
    return defaultDbNameResolver.getLoggingEventColumnName(columnName);
  }

  public String getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName columnName) {
    if (lePropertyColumnNameOverrides.get(columnName) != null)
      return lePropertyColumnNameOverrides.get(columnName);
    return defaultDbNameResolver.getLoggingEventPropertyColumnName(columnName);
  }

  public String getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName columnName) {
    if (leExceptionColumnNameOverrides.get(columnName) != null)
      return leExceptionColumnNameOverrides.get(columnName);
    return defaultDbNameResolver.getLoggingEventExceptionColumnName(columnName);
  }

  public void setLoggingEventTableName(String tableName) {
    tableNameOverrides.put(TableName.LOGGING_EVENT, tableName);
  }

  public void setLoggingEventPropertyTableName(String tableName) {
    tableNameOverrides.put(TableName.LOGGING_EVENT_PROPERTY, tableName);
  }

  public void setLoggingEventExceptionTableName(String tableName) {
    tableNameOverrides.put(TableName.LOGGING_EVENT_EXCEPTION, tableName);
  }

  public void setLoggingEventTimestmpColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.TIMESTMP, columnName);
  }

  public void setLoggingEventFormattedMessageColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.FORMATTED_MESSAGE, columnName);
  }

  public void setLoggingEventLoggerNameColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.LOGGER_NAME, columnName);
  }

  public void setLoggingEventLevelStringColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.LEVEL_STRING, columnName);
  }

  public void setLoggingEventThreadNameColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.THREAD_NAME, columnName);
  }

  public void setLoggingEventReferenceFlagColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.REFERENCE_FLAG, columnName);
  }

  public void setLoggingEventCallerFilenameColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.CALLER_FILENAME, columnName);
  }

  public void setLoggingEventCallerClassColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.CALLER_CLASS, columnName);
  }

  public void setLoggingEventCallerMethodColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.CALLER_METHOD, columnName);
  }

  public void setLoggingEventCallerLineColumnName(String columnName) {
    leColumnNameOverrides.put(LoggingEventColumnName.CALLER_LINE, columnName);
  }

  public void setLoggingEventPropertyEventIdColumnName(String columnName) {
    lePropertyColumnNameOverrides.put(LoggingEventPropertyColumnName.EVENT_ID, columnName);
  }

  public void setLoggingEventPropertyMappedKeyColumnName(String columnName) {
    lePropertyColumnNameOverrides.put(LoggingEventPropertyColumnName.MAPPED_KEY, columnName);
  }

  public void setLoggingEventPropertyMappedValueColumnName(String columnName) {
    lePropertyColumnNameOverrides.put(LoggingEventPropertyColumnName.MAPPED_VALUE, columnName);
  }

  public void setLoggingEventExceptionEventIdColumnName(String columnName) {
    leExceptionColumnNameOverrides.put(LoggingEventExceptionColumnName.EVENT_ID, columnName);
  }

  public void setLoggingEventExceptionIColumnName(String columnName) {
    leExceptionColumnNameOverrides.put(LoggingEventExceptionColumnName.I, columnName);
  }

  public void setLoggingEventExceptionTraceLineColumnName(String columnName) {
    leExceptionColumnNameOverrides.put(LoggingEventExceptionColumnName.TRACE_LINE, columnName);
  }

}
