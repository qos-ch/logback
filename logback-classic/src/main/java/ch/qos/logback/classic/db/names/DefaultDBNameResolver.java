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

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-03-16
 */
public class DefaultDBNameResolver implements DBNameResolver {

  public <N extends Enum<?>> String getTableName(N tableName) {
    return tableName.toString();
  }

  public <N extends Enum<?>> String getColumnName(N columnName) {
    return columnName.toString();
  }

}
