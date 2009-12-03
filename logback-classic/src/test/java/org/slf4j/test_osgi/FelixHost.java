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
package org.slf4j.test_osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * Runs a hosted version of Felix for testing purposes. Any bundle errors are
 * reported via the FrameworkListener passed to the constructor.
 * 
 * @author Ceki G&uuml;c&uuml;
 */
public class FelixHost {

  private Felix felix = null;

  Properties otherProps = new Properties();

  final FrameworkErrorListener frameworkErrorListener;
  final CheckingBundleListener myBundleListener;

  public FelixHost(FrameworkErrorListener frameworkErrorListener,
      CheckingBundleListener myBundleListener) {
    this.frameworkErrorListener = frameworkErrorListener;
    this.myBundleListener = myBundleListener;
  }

  public void doLaunch() {
    // Create a case-insensitive configuration property map.
    Map configMap = new StringMap(false);
    // Configure the Felix instance to be embedded.
    // configMap.put(FelixConstants.EMBEDDED_EXECUTION_PROP, "true");
    // Add core OSGi packages to be exported from the class path
    // via the system bundle.
    configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
        "org.osgi.framework; version=1.3.0,"
            + "org.osgi.service.packageadmin; version=1.2.0,"
            + "org.osgi.service.startlevel; version=1.0.0,"
            + "org.osgi.service.url; version=1.0.0");

    configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN,
        Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

    // Explicitly specify the directory to use for caching bundles.
    // configMap.put(BundleCache.CACHE_PROFILE_DIR_PROP, "cache");

    try {
      // Create host activator;

      List list = new ArrayList();

      // list.add(new HostActivator());
      configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
          "org.xml.sax, org.xml.sax.helpers, javax.xml.parsers, javax.naming");
      configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
      configMap.put("felix.log.level", "4");

      // Now create an instance of the framework with
      // our configuration properties and activator.
      felix = new Felix(configMap);
      felix.init();

      // otherProps.put(Constants.FRAMEWORK_STORAGE, "bundles");

       otherProps.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY,
       AutoProcessor.AUTO_DEPLOY_DIR_VALUE);
      otherProps.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY,
          AutoProcessor.AUTO_DEPLOY_START_VALUE + ","
              + AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE);

      BundleContext felixBudleContext = felix.getBundleContext();

      AutoProcessor.process(otherProps, felixBudleContext);
      // listen to errors
      felixBudleContext.addFrameworkListener(frameworkErrorListener);
      felixBudleContext.addBundleListener(myBundleListener);
      // Now start Felix instance.
      felix.start();
      System.out.println("felix started");

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void stop() throws BundleException {
    felix.stop();
  }

  public Bundle[] getInstalledBundles() {
    // Use the system bundle activator to gain external
    // access to the set of installed bundles.
    return null;// m_activator.getBundles();
  }
}