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

	final static StackTraceElementProxy[] STEP_ARRAY_TEMPLATE = {};

	HashMap<String, ClassPackagingData> cache = new HashMap<>();

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
		} catch (final NoClassDefFoundError | NoSuchMethodError | UnsupportedOperationException e) {
		} catch (final Throwable e) {
			System.err.println("Unexpected exception");
			e.printStackTrace();
		}
	}

	public void calculate(IThrowableProxy tp) {
		while (tp != null) {
			populateFrames(tp.getStackTraceElementProxyArray());
			final IThrowableProxy[] suppressed = tp.getSuppressed();
			if (suppressed != null) {
				for (final IThrowableProxy current : suppressed) {
					populateFrames(current.getStackTraceElementProxyArray());
				}
			}
			tp = tp.getCause();
		}
	}

	@SuppressWarnings("unused")
	void populateFrames(final StackTraceElementProxy[] stepArray) {
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
			final Class<?> callerClass = null;
			if (GET_CALLER_CLASS_METHOD_AVAILABLE) {
				//callerClass = Reflection.getCallerClass(localFirstCommon + i - missfireCount + 1);
			}
			final StackTraceElementProxy step = stepArray[stepFirstCommon + i];
			final String stepClassname = step.ste.getClassName();

			if (callerClass != null && stepClassname.equals(callerClass.getName())) {
				// see also LBCLASSIC-263
				lastExactClassLoader = callerClass.getClassLoader();
				if (firsExactClassLoader == null) {
					firsExactClassLoader = lastExactClassLoader;
				}
				final ClassPackagingData pi = calculateByExactType(callerClass);
				step.setClassPackagingData(pi);
			} else {
				missfireCount++;
				final ClassPackagingData pi = computeBySTEP(step, lastExactClassLoader);
				step.setClassPackagingData(pi);
			}
		}
		populateUncommonFrames(commonFrames, stepArray, firsExactClassLoader);
	}

	void populateUncommonFrames(final int commonFrames, final StackTraceElementProxy[] stepArray, final ClassLoader firstExactClassLoader) {
		final int uncommonFrames = stepArray.length - commonFrames;
		for (int i = 0; i < uncommonFrames; i++) {
			final StackTraceElementProxy step = stepArray[i];
			final ClassPackagingData pi = computeBySTEP(step, firstExactClassLoader);
			step.setClassPackagingData(pi);
		}
	}

	private ClassPackagingData calculateByExactType(final Class<?> type) {
		final String className = type.getName();
		ClassPackagingData cpd = cache.get(className);
		if (cpd != null) {
			return cpd;
		}
		final String version = getImplementationVersion(type);
		final String codeLocation = getCodeLocation(type);
		cpd = new ClassPackagingData(codeLocation, version);
		cache.put(className, cpd);
		return cpd;
	}

	private ClassPackagingData computeBySTEP(final StackTraceElementProxy step, final ClassLoader lastExactClassLoader) {
		final String className = step.ste.getClassName();
		ClassPackagingData cpd = cache.get(className);
		if (cpd != null) {
			return cpd;
		}
		final Class<?> type = bestEffortLoadClass(lastExactClassLoader, className);
		final String version = getImplementationVersion(type);
		final String codeLocation = getCodeLocation(type);
		cpd = new ClassPackagingData(codeLocation, version, false);
		cache.put(className, cpd);
		return cpd;
	}

	String getImplementationVersion(final Class<?> type) {
		if (type == null) {
			return "na";
		}
		final Package aPackage = type.getPackage();
		if (aPackage != null) {
			final String v = aPackage.getImplementationVersion();
			if (v == null) {
				return "na";
			}
			return v;
		}
		return "na";

	}

	String getCodeLocation(final Class<?> type) {
		try {
			if (type != null) {
				// file:/C:/java/maven-2.0.8/repo/com/icegreen/greenmail/1.3/greenmail-1.3.jar
				final CodeSource codeSource = type.getProtectionDomain().getCodeSource();
				if (codeSource != null) {
					final URL resource = codeSource.getLocation();
					if (resource != null) {
						final String locationStr = resource.toString();
						// now lets remove all but the file name
						final String result = getCodeLocation(locationStr, '/');
						if (result != null) {
							return result;
						}
						return getCodeLocation(locationStr, '\\');
					}
				}
			}
		} catch (final Exception e) {
			// ignore
		}
		return "na";
	}

	private String getCodeLocation(final String locationStr, final char separator) {
		int idx = locationStr.lastIndexOf(separator);
		if (isFolder(idx, locationStr)) {
			idx = locationStr.lastIndexOf(separator, idx - 1);
			return locationStr.substring(idx + 1);
		}
		if (idx > 0) {
			return locationStr.substring(idx + 1);
		}
		return null;
	}

	private boolean isFolder(final int idx, final String text) {
		return idx != -1 && idx + 1 == text.length();
	}

	private Class<?> loadClass(final ClassLoader cl, final String className) {
		if (cl == null) {
			return null;
		}
		try {
			return cl.loadClass(className);
		} catch (final ClassNotFoundException | NoClassDefFoundError e1) {
			return null;
		} catch (final Exception e) {
			e.printStackTrace(); // this is unexpected
			return null;
		}

	}

	/**
	 * @param lastGuaranteedClassLoader may be null
	 * @param className
	 * @return
	 */
	private Class<?> bestEffortLoadClass(final ClassLoader lastGuaranteedClassLoader, final String className) {
		Class<?> result = loadClass(lastGuaranteedClassLoader, className);
		if (result != null) {
			return result;
		}
		final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		if (tccl != lastGuaranteedClassLoader) {
			result = loadClass(tccl, className);
		}
		if (result != null) {
			return result;
		}

		try {
			return Class.forName(className);
		} catch (final ClassNotFoundException | NoClassDefFoundError e1) {
			return null;
		} catch (final Exception e) {
			e.printStackTrace(); // this is unexpected
			return null;
		}
	}

}
