/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import sun.reflect.Reflection;
import ch.qos.logback.classic.spi.ThrowableDataPoint.ThrowableDataPointType;

/**
 * 
 * Given a classname locate associated PackageInfo (jar name, version name).
 * 
 * @author James Strachan
 * @Ceki G&uuml;lc&uuml;
 */
public class ClassPackagingDataCalculator {

  final static StackTraceElementProxy[] STEP_ARRAY_TEMPLATE = new StackTraceElementProxy[0];

  HashMap<String, ClassPackagingData> cache = new HashMap<String, ClassPackagingData>();

  public ClassPackagingDataCalculator() {
  }

  public void calculate(ThrowableDataPoint[] tdpArray) {
    int steStart = 0;
    StackTraceElementProxy[] stepArray = new StackTraceElementProxy[0];
    do  {
      steStart = findSTEStartIndex(tdpArray, steStart+stepArray.length);
      stepArray = getSTEPArray(tdpArray, steStart);
      populateFrames(stepArray);
    } while(steStart != -1);
  }

  void populateFrames(StackTraceElementProxy[] stepArray) {
    // in the initial part of this method we populate package informnation for 
    // common stack frames
    final Throwable t = new Throwable("local stack reference");
    final StackTraceElement[] localteSTEArray = t.getStackTrace();
    final int commonFrames = STEUtil.findNumberOfCommonFrames(localteSTEArray,
        stepArray);
    final int localFirstCommon = localteSTEArray.length - commonFrames;
    final int stepFirstCommon = stepArray.length - commonFrames;

    ClassLoader lastExactClassLoader = null;
    ClassLoader firsExactClassLoader = null;
    
    int missfireCount = 0;
    for (int i = 0; i < commonFrames; i++) {
      Class callerClass = Reflection.getCallerClass(localFirstCommon + i
          - missfireCount + 1);
      StackTraceElementProxy step = stepArray[stepFirstCommon + i];
      String stepClassname = step.ste.getClassName();
      
      if (stepClassname.equals(callerClass.getName())) {
        lastExactClassLoader = callerClass.getClassLoader();
        if(firsExactClassLoader == null) {
          firsExactClassLoader = callerClass.getClassLoader();
        }
        ClassPackagingData pi = calculateByExactType(callerClass);
        step.setPackageInfo(pi);
      } else {
        missfireCount++;
        ClassPackagingData pi = computeBySTEP(step, lastExactClassLoader);
        step.setPackageInfo(pi);
      }
    }
    populateUncommonFrames(commonFrames, stepArray, firsExactClassLoader);
  }

  int findSTEStartIndex(final ThrowableDataPoint[] tdpArray, final int from) {
    final int len = tdpArray.length;
    if (from < 0 || from >= len) {
      return -1;
    }
    for (int i = from; i < len; i++) {
      if (tdpArray[i].type == ThrowableDataPointType.STEP) {
        return i;
      }
    }
    return -1;
  }
  
  private StackTraceElementProxy[] getSTEPArray(final ThrowableDataPoint[] tdpArray, final int from) {
    List<StackTraceElementProxy> stepList = new LinkedList<StackTraceElementProxy>();
    int len = tdpArray.length;
    if (from < 0 || from >= len) {
      return stepList.toArray(STEP_ARRAY_TEMPLATE);
    }
    for (int i = from; i < len; i++) {
      final ThrowableDataPoint tdp = tdpArray[i];

      if (tdp.type == ThrowableDataPointType.STEP) {
        stepList.add(tdp.getStackTraceElementProxy());
      } else {
        break;
      }
    }
    return stepList.toArray(STEP_ARRAY_TEMPLATE);
  }
  
  void populateUncommonFrames(int commonFrames, StackTraceElementProxy[] stepArray, ClassLoader firstExactClassLoader) {
    int uncommonFrames = stepArray.length-commonFrames;
    for (int i = 0; i < uncommonFrames; i++) {
      StackTraceElementProxy step = stepArray[i];
      ClassPackagingData pi = computeBySTEP(step, firstExactClassLoader);
      step.setPackageInfo(pi);
    }
  }

  private ClassPackagingData calculateByExactType(Class type) {
    String className = type.getName();
    ClassPackagingData cpd = cache.get(className);
    if (cpd != null) {
      return cpd;
    }
    String version = getImplementationVersion(type);
    String codeLocation = getCodeLocation(type);
    cpd = new ClassPackagingData(codeLocation, version);
    cache.put(className, cpd);
    return cpd;
  }

  private ClassPackagingData computeBySTEP(StackTraceElementProxy step, ClassLoader lastExactClassLoader) {
    String className = step.ste.getClassName();
    ClassPackagingData cpd = cache.get(className);
    if (cpd != null) {
      return cpd;
    }
    Class type = bestEffortLoadClass(lastExactClassLoader, className);
    String version = getImplementationVersion(type);
    String codeLocation = getCodeLocation(type);
    cpd = new ClassPackagingData(codeLocation, version, false);
    cache.put(className, cpd);
    return cpd;
  }
  
  
  String getImplementationVersion(Class type) {
    Package aPackage = type.getPackage();
    if (aPackage != null) {
      String v = aPackage.getImplementationVersion();
      if (v == null) {
        return "na";
      } else {
        return v;
      }
    }
    return "na";

  }

  String getCodeLocation(Class type) {
    try {
      if (type != null) {
        // file:/C:/java/maven-2.0.8/repo/com/icegreen/greenmail/1.3/greenmail-1.3.jar
        URL resource = type.getProtectionDomain().getCodeSource().getLocation();
        if (resource != null) {
          String locationStr = resource.toString();
          // now lets remove all but the file name
          String result = getCodeLocation(locationStr, '/');
          if(result != null) {
            return result;
          }
          return getCodeLocation(locationStr, '\\');
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return "na";
  }
  

  private String getCodeLocation(String locationStr, char separator) {
    int idx = locationStr.lastIndexOf(separator);
    if(isFolder(idx, locationStr)) {
      idx = locationStr.lastIndexOf(separator, idx-1);
      return locationStr.substring(idx+1);
    } else if (idx > 0) {
      return locationStr.substring(idx + 1);
    }
    return null;
  }

  private boolean isFolder(int idx, String text) {
    return (idx != -1 && idx+1 == text.length());
  }
  
  private Class loadClass(ClassLoader cl, String className) {
    if(cl == null) {
      return null;
    }
    try {
      return cl.loadClass(className);
    } catch (ClassNotFoundException e1) {
      return null;
    }  catch(Exception e) {
      e.printStackTrace(); // this is unexpected
      return null;
    }
    
  }
  
  /**
   * 
   * @param lastGuaranteedClassLoader may be null
   * @param className
   * @return
   */
  private Class bestEffortLoadClass(ClassLoader lastGuaranteedClassLoader, String className) {
    Class result = loadClass(lastGuaranteedClassLoader, className);
    if(result != null) {
      return result;
    }
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    if(tccl != lastGuaranteedClassLoader) {
      result = loadClass(tccl, className);
    }
    if(result != null) {
      return result;
    }
    
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e1) {
      return null;
    } catch(Exception e) {
      e.printStackTrace(); // this is unexpected
      return null;
    }
  }

}
