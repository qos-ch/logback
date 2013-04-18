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
 * The default name resolver simply returns the enum passes as parameter
 * as a lower case string.
 * 
 * @author Tomasz Nurkiewicz
 * @author Ceki Gulcu
 * @since  0.9.19
 */
public class DefaultDBNameResolver implements DBNameResolver {

  public <N extends Enum<?>> String getTableName(N tableName) {
    return tableName.toString().toLowerCase();
  }

  public <N extends Enum<?>> String getColumnName(N columnName) {
    return columnName.toString().toLowerCase();
  }

}
