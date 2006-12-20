/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j;

import java.util.Hashtable;

/**
 * This class is a factory that creates and maintains org.apache.log4j.Loggers
 * warpping org.slf4j.Loggers.
 * 
 * It keeps a hashtable of all created org.apache.log4j.Logger instances so that
 * all newly created instances are not dulpicates of existing loggers.
 * 
 * @author S&eacute;bastien Pennec
 */
class Log4jLoggerFactory {

  private static Hashtable<String, Logger> log4jLoggers = new Hashtable<String, Logger>();

  public static synchronized Logger getLogger(String name) {
    if (log4jLoggers.containsKey(name)) {
      return (org.apache.log4j.Logger) log4jLoggers.get(name);
    } else {
      Logger log4jLogger = new Logger(name);
      
      log4jLoggers.put(name, log4jLogger);
      return log4jLogger;
    }
  }

}
