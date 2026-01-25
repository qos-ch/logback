/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.spi;


import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import ch.qos.logback.core.spi.LifeCycle;

public class NoAutoStartUtil {

    /**
     * Returns true if the class of the object 'o' passed as parameter is *not*
     * marked with the NoAutoStart annotation. Return true otherwise.
     *
     * @param o
     * @return true for classes not marked with the NoAutoStart annotation
     */
    public static boolean notMarkedWithNoAutoStart(Object o) {
        if (o == null) {
            return false;
        }
        Class<?> clazz = o.getClass();
        NoAutoStart a = findAnnotation(clazz, NoAutoStart.class);
        return a == null;
    }

	/**
	 * Find a single {@link Annotation} of {@code annotationType} on the
	 * supplied {@link Class}, traversing its interfaces, annotations, and
	 * superclasses if the annotation is not <em>directly present</em> on
	 * the given class itself.
	 * <p>This method explicitly handles class-level annotations which are not
	 * declared as {@link java.lang.annotation.Inherited inherited} <em>as well
	 * as meta-annotations and annotations on interfaces</em>.
	 * <p>The algorithm operates as follows:
	 * <ol>
	 * <li>Search for the annotation on the given class and return it if found.
	 * <li>Recursively search through all interfaces that the given class declares.
	 * <li>Recursively search through the superclass hierarchy of the given class.
	 * </ol>
	 * <p>Note: in this context, the term <em>recursively</em> means that the search
	 * process continues by returning to step #1 with the current interface,
	 * annotation, or superclass as the class to look for annotations on.
	 * @param clazz the class to look for annotations on
	 * @param annotationType the type of annotation to look for
	 * @return the first matching annotation, or {@code null} if not found
	 */
	private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
		return findAnnotation(clazz, annotationType, new HashSet<>());
	}

	/**
	 * Perform the search algorithm for {@link #findAnnotation(Class, Class)},
	 * avoiding endless recursion by tracking which annotations have already
	 * been <em>visited</em>.
	 * @param clazz the class to look for annotations on
	 * @param annotationType the type of annotation to look for
	 * @param visited the set of annotations that have already been visited
	 * @return the first matching annotation, or {@code null} if not found
	 */
	@SuppressWarnings("unchecked")
	private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType, Set<Annotation> visited) {

		Annotation foundAnnotation = clazz.getAnnotation(annotationType);
		if(foundAnnotation != null) {
			return (A) foundAnnotation;
		}


		for (Class<?> ifc : clazz.getInterfaces()) {
			A annotation = findAnnotation(ifc, annotationType, visited);
			if (annotation != null) {
				return annotation;
			}
		}

		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null || Object.class == superclass) {
			return null;
		}
		return findAnnotation(superclass, annotationType, visited);
	}

  /**
     * Is the object a {@link LifeCycle} and is it marked not marked with
     * the NoAutoStart annotation.
     * @param o
     * @return
     * @ since 1.5.2
     */
    static public boolean shouldBeStarted(Object o) {
        if(o instanceof LifeCycle) {
            return notMarkedWithNoAutoStart(o);
        } else
            return false;
    }
}
