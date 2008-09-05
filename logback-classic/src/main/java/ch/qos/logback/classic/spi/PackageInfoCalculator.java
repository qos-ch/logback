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
public class PackageInfoCalculator {

  final static StackTraceElementProxy[] STEP_ARRAY_TEMPLATE = new StackTraceElementProxy[0];
  
  HashMap<String, PackageInfo> cache = new HashMap<String, PackageInfo>();

 
  PackageInfoCalculator() {
    
  }
  
  public void computePackageInfo(ThrowableDataPoint[] tdpArray) {
  
    int steStart= findSTEStartIndex(tdpArray, 0);
    StackTraceElementProxy[] stepArray = getSTEPArray(tdpArray, steStart);
    populateWithPackageInfo(stepArray);
    
  }
  
  void populateWithPackageInfo(StackTraceElementProxy[] stepArray) {
    Throwable t = new Throwable("local");
    //t.printStackTrace();
    StackTraceElement[] localteSTEArray = t.getStackTrace();
    int commonFrames = STEUtil.findNumberOfCommonFrames(localteSTEArray, stepArray);
  
    int  localFirstCommon = localteSTEArray.length-commonFrames;
    int  stepFirstCommon = stepArray.length-commonFrames;
    
    //System.out.println("commonFr4ames="+commonFrames);
    
    int missfireCount = 0;
    for(int i = 0; i < commonFrames; i++) {
      Class callerClass = Reflection.getCallerClass(localFirstCommon+i-missfireCount+1);
      StackTraceElementProxy step =  stepArray[stepFirstCommon+i];
      String stepClassname = step.ste.getClassName();
      //System.out.println("step.class = "+stepClassname);
      
      if(!stepClassname.equals(callerClass.getName())) {
        missfireCount++;
        PackageInfo pi = compute(step);
        step.setPackageInfo(pi);
      } else {
        PackageInfo pi = computeByType(callerClass);
        step.setPackageInfo(pi);
      }  
    }
    
    populateX(commonFrames, stepArray); 
    
  }
  
  int findSTEStartIndex(ThrowableDataPoint[] tdpArray, int from) {
    int len = tdpArray.length;
    if(from < 0 || from >= len) {
      return -1;
    }
    for(int i = from; i < len; i++) {
      if(tdpArray[i].type == ThrowableDataPointType.STEP) {
        return i;
      }
    }
    return -1;
  }
  
  void populateX(int commonFrames, StackTraceElementProxy[] stepArray) {
    for(int i = 0; i < commonFrames; i++) {
      StackTraceElementProxy step = stepArray[i];
      PackageInfo pi = compute(step);
      step.setPackageInfo(pi);
    }
  }
  
  StackTraceElementProxy[] getSTEPArray(ThrowableDataPoint[] tdpArray, int from) {
    List<StackTraceElementProxy> stepList = new LinkedList<StackTraceElementProxy>();
    int len = tdpArray.length;
    if(from < 0 || from >= len) {
      return stepList.toArray(STEP_ARRAY_TEMPLATE);
    }
    for(int i = from; i < len; i++) {
      final ThrowableDataPoint tdp = tdpArray[i];
      
      if(tdp.type == ThrowableDataPointType.STEP) {
        stepList.add(tdp.getStackTraceElementProxy());
      } else {
        break;
      }
    }
    return stepList.toArray(STEP_ARRAY_TEMPLATE);
  }

  private PackageInfo computeByType(Class type) {
    String className = type.getName();
    PackageInfo pi = cache.get(className);
    if (pi != null) {
      return pi;
    }
    String version = getVersion(className);
    String jarname = getJarNameOfClass(type);
    pi = new PackageInfo(jarname, version);
    cache.put(className, pi);
    return pi;
  }
  
  private PackageInfo compute(StackTraceElementProxy step) {
    String className = step.ste.getClassName();
    PackageInfo pi = cache.get(className);
    if (pi != null) {
      return pi;
    }
    String version = getVersion(className);
    Class type = bestEffortFindClass(className);
    String jarname = getJarNameOfClass(type);
    pi = new PackageInfo(jarname, version);
    cache.put(className, pi);
    return pi;
  }

  String getVersion(String className) {
    String packageName = getPackageName(className);
    Package aPackage = Package.getPackage(packageName);
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

  static String getPackageName(String className) {
    int j = className.lastIndexOf('.');
    return className.substring(0, j);
  }

  String getJarNameOfClass(Class type) {
    try {
      if (type != null) {

        // file:/C:/java/maven-2.0.8/repo/com/icegreen/greenmail/1.3/greenmail-1.3.jar
        URL resource = type.getProtectionDomain().getCodeSource().getLocation();
        if (resource != null) {
          String text = resource.toString();
          // now lets remove all but the file name
          int idx = text.lastIndexOf('/');
          if (idx > 0) {
            text = text.substring(idx + 1);
          }
          idx = text.lastIndexOf('\\');
          if (idx > 0) {
            text = text.substring(idx + 1);
          }
          return text;
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return "na";
  }

  
  private Class bestEffortFindClass(String className) {
    try {
      return Thread.currentThread().getContextClassLoader()
          .loadClass(className);
    } catch (ClassNotFoundException e) {
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e1) {
        return null;
      }
    }
  }

}
