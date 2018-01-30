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
package ch.qos.logback.classic.spi;

import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;

//import sun.reflect.Reflection;

// import java.security.AccessControlException; import java.security.AccessController;import java.security.PrivilegedAction;
/**
 * Given a classname locate associated PackageInfo (jar name, version name).
 *
 * @author James Strachan
 * @Ceki G&uuml;lc&uuml;
 */
public class PackagingDataCalculator {

    final static StackTraceElementProxy[] STEP_ARRAY_TEMPLATE = new StackTraceElementProxy[0];

    HashMap<String, ClassPackagingData> cache = new HashMap<String, ClassPackagingData>();

    private static boolean GET_CALLER_CLASS_METHOD_AVAILABLE = false; // private static boolean
                                                                      // HAS_GET_CLASS_LOADER_PERMISSION = false;

    static {
        // if either the Reflection class or the getCallerClass method
        // are unavailable, then we won't invoke Reflection.getCallerClass()
        // This approach ensures that this class will *run* on JDK's lacking
        // sun.reflect.Reflection class. However, this class will *not compile*
        // on JDKs lacking sun.reflect.Reflection.
        try {
            //Reflection.getCallerClass(2);
            //GET_CALLER_CLASS_METHOD_AVAILABLE = true;
        } catch (NoClassDefFoundError e) {
        } catch (NoSuchMethodError e) {
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            System.err.println("Unexpected exception");
            e.printStackTrace();
        }
    }

    public void calculate(IThrowableProxy tp) {
        while (tp != null) {
            populateFrames(tp.getStackTraceElementProxyArray());
            IThrowableProxy[] suppressed = tp.getSuppressed();
            if (suppressed != null) {
                for (IThrowableProxy current : suppressed) {
                    populateFrames(current.getStackTraceElementProxyArray());
                }
            }
            tp = tp.getCause();
        }
    }

    @SuppressWarnings("unused")
    void populateFrames(StackTraceElementProxy[] stepArray) {
        // in the initial part of this method we populate package information for
        // common stack frames
        final Throwable t = new Throwable("local stack reference");
        final StackTraceElement[] localSTEArray = t.getStackTrace();
        final int commonFrames = STEUtil.findNumberOfCommonFrames(localSTEArray, stepArray);
        final int localFirstCommon = localSTEArray.length - commonFrames;
        final int stepFirstCommon = stepArray.length - commonFrames;

        ClassLoader lastExactClassLoader = null;
        ClassLoader firsExactClassLoader = null;

        int missfireCount = 0;
        for (int i = 0; i < commonFrames; i++) {
            Class<?> callerClass = null;
            if (GET_CALLER_CLASS_METHOD_AVAILABLE) {
                //callerClass = Reflection.getCallerClass(localFirstCommon + i - missfireCount + 1);
            }
            StackTraceElementProxy step = stepArray[stepFirstCommon + i];
            String stepClassname = step.ste.getClassName();

            if (callerClass != null && stepClassname.equals(callerClass.getName())) {
                // see also LBCLASSIC-263
                lastExactClassLoader = callerClass.getClassLoader();
                if (firsExactClassLoader == null) {
                    firsExactClassLoader = lastExactClassLoader;
                }
                ClassPackagingData pi = calculateByExactType(callerClass);
                step.setClassPackagingData(pi);
            } else {
                missfireCount++;
                ClassPackagingData pi = computeBySTEP(step, lastExactClassLoader);
                step.setClassPackagingData(pi);
            }
        }
        populateUncommonFrames(commonFrames, stepArray, firsExactClassLoader);
    }

    void populateUncommonFrames(int commonFrames, StackTraceElementProxy[] stepArray, ClassLoader firstExactClassLoader) {
        int uncommonFrames = stepArray.length - commonFrames;
        for (int i = 0; i < uncommonFrames; i++) {
            StackTraceElementProxy step = stepArray[i];
            ClassPackagingData pi = computeBySTEP(step, firstExactClassLoader);
            step.setClassPackagingData(pi);
        }
    }

    private ClassPackagingData calculateByExactType(Class<?> type) {
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
        Class<?> type = bestEffortLoadClass(lastExactClassLoader, className);
        String version = getImplementationVersion(type);
        String codeLocation = getCodeLocation(type);
        cpd = new ClassPackagingData(codeLocation, version, false);
        cache.put(className, cpd);
        return cpd;
    }

    String getImplementationVersion(Class<?> type) {
        if (type == null) {
            return "na";
        }
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

    String getCodeLocation(Class<?> type) {
        try {
            if (type != null) {
                // file:/C:/java/maven-2.0.8/repo/com/icegreen/greenmail/1.3/greenmail-1.3.jar
                CodeSource codeSource = type.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    URL resource = codeSource.getLocation();
                    if (resource != null) {
                        String locationStr = resource.toString();
                        // now lets remove all but the file name
                        String result = getCodeLocation(locationStr, '/');
                        if (result != null) {
                            return result;
                        }
                        return getCodeLocation(locationStr, '\\');
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "na";
    }

    private String getCodeLocation(String locationStr, char separator) {
        int idx = locationStr.lastIndexOf(separator);
        if (isFolder(idx, locationStr)) {
            idx = locationStr.lastIndexOf(separator, idx - 1);
            return locationStr.substring(idx + 1);
        } else if (idx > 0) {
            return locationStr.substring(idx + 1);
        }
        return null;
    }

    private boolean isFolder(int idx, String text) {
        return (idx != -1 && idx + 1 == text.length());
    }

    private Class<?> loadClass(ClassLoader cl, String className) {
        if (cl == null) {
            return null;
        }
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e1) {
            return null;
        } catch (NoClassDefFoundError e1) {
            return null;
        } catch (Exception e) {
            e.printStackTrace(); // this is unexpected
            return null;
        }

    }

    /**
     * @param lastGuaranteedClassLoader may be null
     * @param className
     * @return
     */
    private Class<?> bestEffortLoadClass(ClassLoader lastGuaranteedClassLoader, String className) {
        Class<?> result = loadClass(lastGuaranteedClassLoader, className);
        if (result != null) {
            return result;
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != lastGuaranteedClassLoader) {
            result = loadClass(tccl, className);
        }
        if (result != null) {
            return result;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e1) {
            return null;
        } catch (NoClassDefFoundError e1) {
            return null;
        } catch (Exception e) {
            e.printStackTrace(); // this is unexpected
            return null;
        }
    }

}
