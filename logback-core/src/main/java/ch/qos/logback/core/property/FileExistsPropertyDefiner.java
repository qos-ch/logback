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
package ch.qos.logback.core.property;

import ch.qos.logback.core.PropertyDefinerBase;
import java.io.File;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class FileExistsPropertyDefiner extends PropertyDefinerBase {

  String path;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPropertyValue() {
    if(path == null)
      return "false";
    File file = new File(path);
    System.out.println(file.getAbsolutePath());
    if(file.exists())
      return "true";
    else
    return "false";
  }
}
