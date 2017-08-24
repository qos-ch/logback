/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.db.dialect;

/**
 * SQLite dialect
 *
 * Note that the dialect is not needed if your JDBC driver supports the
 * getGeneratedKeys method introduced in JDBC 3.0 specification.
 *
 * @author Anthony Trinh
 */
public class SQLiteDialect implements SQLDialect {
    public static final String SELECT_CURRVAL = "SELECT last_insert_rowid();";

    public String getSelectInsertId() {
        return SELECT_CURRVAL;
    }
}
