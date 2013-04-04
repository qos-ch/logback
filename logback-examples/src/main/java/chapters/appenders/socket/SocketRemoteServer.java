/*
 * File created on Apr 2, 2013 
 *
 * Copyright 2008-2011 Virginia Polytechnic Institute and State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package chapters.appenders.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * This application loads a configuration containing a 
 * {@link ServerSocketAppender} and then allows the user to enter messages
 * which will be relayed to remote clients via this appender.
 */
public class SocketRemoteServer {

  static void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + SocketRemoteServer.class.getName() +
      " configFile\n" +
      "   configFile a logback configuration file" +
      "   in XML format.");
    System.exit(1);
  }

  static public void main(String[] args) throws Exception {
    if (args.length != 1) {
      usage("Wrong number of arguments.");
    }

    String configFile = args[0];

    if (configFile.endsWith(".xml")) {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      lc.stop();
      configurator.setContext(lc);
      configurator.doConfigure(configFile);
    }

    Logger logger = LoggerFactory.getLogger(SocketRemoteServer.class);

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      System.out.println(
        "Type a message to send to remote clients. Type 'q' to quit.");

      String s = reader.readLine();

      if (s.equals("q")) {
        break;
      } else {
        logger.debug(s);
      }
    }
    
    ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
  }

}
