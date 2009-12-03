/*
 * Copyright (c) 2004-2009 QOS.ch All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS  IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package integrator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A BundleActivator which invokes slf4j loggers
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class Activator implements BundleActivator {

  private BundleContext m_context = null;

  public void start(BundleContext context) {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    
    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      // the context was probably already configured by default configuration 
      // rules
      lc.reset(); 
      configurator.doConfigure("src/test/input/osgi/simple.xml");
    } catch (JoranException je) {
       je.printStackTrace();
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

    
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.info("Activator.start()");
    m_context = context;
  }

  public void stop(BundleContext context) {
    m_context = null;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.info("Activator.stop");
  }

  public Bundle[] getBundles() {
    if (m_context != null) {
      return m_context.getBundles();
    }
    return null;
  }
}