/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.db.names;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-16
 */
public class CustomDBNameResolver implements DBNameResolver {

  private final DBNameResolver defaultDbNameResolver = new DefaultDBNameResolver();
  private final Map<String, String> tableNameOverrides = new HashMap<String, String>();
  private final Map<String, String> columnNameOverrides = new HashMap<String, String>();
  
  public <N extends Enum<?>> String getTableName(N tableName) {
    if (tableNameOverrides.get(tableName.name()) != null)
      return tableNameOverrides.get(tableName.name());
    return defaultDbNameResolver.getTableName(tableName);
  }

  public <N extends Enum<?>> String getColumnName(N columnName) {
    if (columnNameOverrides.get(columnName.name()) != null)
      return columnNameOverrides.get(columnName.name());
    return defaultDbNameResolver.getColumnName(columnName);
  }

  public void overrideTableName(String reference, String name) {
    tableNameOverrides.put(reference, name);
  }
  public void overrideColumnName(String reference, String name) {
    columnNameOverrides.put(reference, name);
  }

  public <N extends Enum<?>> void overrideTableName(N referenceTableName, String name) {
    tableNameOverrides.put(referenceTableName.name(), name);
  }
  public <N extends Enum<?>> void overrideColumnName(N referenceColumnName, String name) {
    columnNameOverrides.put(referenceColumnName.name(), name);
  }


  

}
