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
 * The Oracle dialect. Tested successfully on Oracle9i Release 9.2.0.3.0 by 
 * James Stauffer.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class OracleDialect implements SQLDialect {
    public static final String SELECT_CURRVAL = "SELECT logging_event_id_seq.currval from dual";

    public String getSelectInsertId() {
        return SELECT_CURRVAL;
    }

}
