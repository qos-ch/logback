package ch.qos.logback.classic.db;

import ch.qos.logback.classic.db.names.*;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-16
 */
public class SQLBuilder {

  static String buildInsertPropertiesSQL(DBNameResolver dbNameResolver) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
    sqlBuilder.append(dbNameResolver.getTableName(TableName.LOGGING_EVENT_PROPERTY)).append(" (");
    sqlBuilder.append(dbNameResolver.getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName.EVENT_ID)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName.MAPPED_KEY)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName.MAPPED_VALUE)).append(") ");
    sqlBuilder.append("VALUES (?, ?, ?)");
    return sqlBuilder.toString();
  }

  static String buildInsertExceptionSQL(DBNameResolver dbNameResolver) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
    sqlBuilder.append(dbNameResolver.getTableName(TableName.LOGGING_EVENT_EXCEPTION)).append(" (");
    sqlBuilder.append(dbNameResolver.getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName.EVENT_ID)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName.I)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName.TRACE_LINE)).append(") ");
    sqlBuilder.append("VALUES (?, ?, ?)");
    return sqlBuilder.toString();
  }

  static String buildInsertSQL(DBNameResolver dbNameResolver) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
    sqlBuilder.append(dbNameResolver.getTableName(TableName.LOGGING_EVENT)).append(" (");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.TIMESTMP)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.FORMATTED_MESSAGE)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.LOGGER_NAME)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.LEVEL_STRING)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.THREAD_NAME)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.REFERENCE_FLAG)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.CALLER_FILENAME)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.CALLER_CLASS)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.CALLER_METHOD)).append(", ");
    sqlBuilder.append(dbNameResolver.getLoggingEventColumnName(LoggingEventColumnName.CALLER_LINE)).append(") ");
    sqlBuilder.append("VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?)");
    return sqlBuilder.toString();
  }
}
