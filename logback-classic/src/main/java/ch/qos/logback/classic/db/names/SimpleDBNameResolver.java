package ch.qos.logback.classic.db.names;

/**
 * TODO: Handle null prefixes/suffixes somehow
 * @author Tomasz Nurkiewicz
 * @since 2010-03-19
 */
public class SimpleDBNameResolver implements DBNameResolver {

  private String tableNamePrefix = "";
  private String tableNameSuffix = "";
  private String columnNamePrefix = "";
  private String columnNameSuffix = "";

  public String getTableName(TableName tableName) {
    return tableNamePrefix + tableName.name() + columnNamePrefix;
  }

  public String getLoggingEventColumnName(LoggingEventColumnName columnName) {
    return columnNamePrefix + columnName.name() + columnNameSuffix;
  }

  public String getLoggingEventPropertyColumnName(LoggingEventPropertyColumnName columnName) {
    return columnNamePrefix + columnName.name() + columnNameSuffix;
  }

  public String getLoggingEventExceptionColumnName(LoggingEventExceptionColumnName columnName) {
    return columnNamePrefix + columnName.name() + columnNameSuffix;
  }

  public void setTableNamePrefix(String tableNamePrefix) {
    this.tableNamePrefix = tableNamePrefix;
  }

  public void setTableNameSuffix(String tableNameSuffix) {
    this.tableNameSuffix = tableNameSuffix;
  }

  public void setColumnNamePrefix(String columnNamePrefix) {
    this.columnNamePrefix = columnNamePrefix;
  }

  public void setColumnNameSuffix(String columnNameSuffix) {
    this.columnNameSuffix = columnNameSuffix;
  }
}
