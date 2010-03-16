package ch.qos.logback.classic.db.names;

public interface DBNameResolver {

  String getTableName(TableName tableName);

  String getLoggingEventColumnName(LoggingEventColumnName columnName);

  String getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName columnName);

  String getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName columnName);

}