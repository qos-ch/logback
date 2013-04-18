/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.db.names;

/**
 * Adds custom prefix/suffix to table and column names.
 *
 * @author Tomasz Nurkiewicz
 * @since 0.9.20
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
