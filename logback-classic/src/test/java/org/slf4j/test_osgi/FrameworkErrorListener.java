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

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class FrameworkErrorListener implements FrameworkListener {

  public List errorList = new ArrayList();
  
  public void frameworkEvent(FrameworkEvent fe) {
    if (fe.getType() == FrameworkEvent.ERROR) {
      errorList.add(fe);
    
    }
  }
  
  private void dump(FrameworkEvent fe) {
    Throwable t = fe.getThrowable();
    String tString = null;
    if (t != null) {
      tString = t.toString();
    }
    System.out.println("Framework ERROR:" + ", source " + fe.getSource()
        + ", bundle=" + fe.getBundle() + ", ex=" + tString);
    if(t != null) {
      t.printStackTrace();
    }
  }

  public void dumpAll() {
    for(int i = 0; i < errorList.size(); i++) {
      FrameworkEvent fe = (FrameworkEvent) errorList.get(i);
      dump(fe);
    }
  }
}
