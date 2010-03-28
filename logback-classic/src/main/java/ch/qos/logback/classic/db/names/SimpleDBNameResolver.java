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

  public <N extends Enum<?>> String getTableName(N tableName) {
    return tableNamePrefix + tableName.name().toLowerCase() + tableNameSuffix;
  }

  public <N extends Enum<?>> String getColumnName(N columnName) {
    return columnNamePrefix + columnName.name().toLowerCase() + columnNameSuffix;
  }

  public void setTableNamePrefix(String tableNamePrefix) {
    this.tableNamePrefix = tableNamePrefix != null? tableNamePrefix : "";
  }

  public void setTableNameSuffix(String tableNameSuffix) {
    this.tableNameSuffix = tableNameSuffix != null? tableNameSuffix : "";
  }

  public void setColumnNamePrefix(String columnNamePrefix) {
    this.columnNamePrefix = columnNamePrefix != null? columnNamePrefix : "";
  }

  public void setColumnNameSuffix(String columnNameSuffix) {
    this.columnNameSuffix = columnNameSuffix != null? columnNameSuffix : "";
  }
}
