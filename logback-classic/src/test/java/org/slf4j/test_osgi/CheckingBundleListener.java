package org.slf4j.test_osgi;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class CheckingBundleListener implements BundleListener {

  List eventList = new ArrayList();

  public void bundleChanged(BundleEvent be) {
    eventList.add(be);
  }

  private void dump(BundleEvent be) {
    System.out.println("BE:" + ", source " + be.getSource() + ", bundle="
        + be.getBundle() + ", type=" + be.getType());

  }

  public void dumpAll() {
    for (int i = 0; i < eventList.size(); i++) {
      BundleEvent fe = (BundleEvent) eventList.get(i);
      dump(fe);
    }
  }

  boolean exists(String bundleName) {
    for (int i = 0; i < eventList.size(); i++) {
      BundleEvent fe = (BundleEvent) eventList.get(i);
      Bundle b = fe.getBundle();
      System.out.println("===["+b+"]");
      if (bundleName.equals(b.getSymbolicName())) {
        return true;
      }
    }
    return false;
  }

}
