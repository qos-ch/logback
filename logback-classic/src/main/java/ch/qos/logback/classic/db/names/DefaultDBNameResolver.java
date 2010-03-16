package ch.qos.logback.classic.db.names;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-16
 */
public class DefaultDBNameResolver implements DBNameResolver {
  public String getTableName(TableName tableName) {
    return tableName.name().toLowerCase();
  }

  public String getLoggingEventColumnName(LoggingEventColumnName columnName) {
    return columnName.name().toLowerCase();
  }

  public String getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName columnName) {
    return columnName.name().toLowerCase();
  }

  public String getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName columnName) {
    return columnName.name().toLowerCase();
  }
}
