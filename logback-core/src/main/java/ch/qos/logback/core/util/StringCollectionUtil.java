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
package ch.qos.logback.core.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Static utility methods for working with collections of strings.
 *
 * @author Carl Harris
 */
public class StringCollectionUtil {

    /**
     * Retains all values in the subject collection that are matched by
     * at least one of a collection of regular expressions.
     * <p>
     * This method is a convenience overload for
     * {@link #retainMatching(Collection, Collection)}.
     *   
     * @param values subject value collection 
     * @param patterns patterns to match
     */
    public static void retainMatching(Collection<String> values, String... patterns) {
        retainMatching(values, Arrays.asList(patterns));
    }

    /**
     * Retains all values in the subject collection that are matched by
     * at least one of a collection of regular expressions.
     * <p>
     * The semantics of this method are conceptually similar to
     * {@link Collection#retainAll(Collection)}, but uses pattern matching
     * instead of exact matching.
     * 
     * @param values subject value collection 
     * @param patterns patterns to match
     */
    public static void retainMatching(Collection<String> values, Collection<String> patterns) {
        if (patterns.isEmpty())
            return;
        List<String> matches = new ArrayList<String>(values.size());
        for (String p : patterns) {
            Pattern pattern = Pattern.compile(p);
            for (String value : values) {
                if (pattern.matcher(value).matches()) {
                    matches.add(value);
                }
            }
        }
        values.retainAll(matches);
    }

    /**
     * Removes all values in the subject collection that are matched by
     * at least one of a collection of regular expressions.
     * <p>
     * This method is a convenience overload for
     * {@link #removeMatching(Collection, Collection)}.
     * 
     * @param values subject value collection 
     * @param patterns patterns to match
     */
    public static void removeMatching(Collection<String> values, String... patterns) {
        removeMatching(values, Arrays.asList(patterns));
    }

    /**
     * Removes all values in the subject collection that are matched by
     * at least one of a collection of regular expressions.
     * <p>
     * The semantics of this method are conceptually similar to
     * {@link Collection#removeAll(Collection)}, but uses pattern matching
     * instead of exact matching.
     * 
     * @param values subject value collection 
     * @param patterns patterns to match
     */
    public static void removeMatching(Collection<String> values, Collection<String> patterns) {
        List<String> matches = new ArrayList<String>(values.size());
        for (String p : patterns) {
            Pattern pattern = Pattern.compile(p);
            for (String value : values) {
                if (pattern.matcher(value).matches()) {
                    matches.add(value);
                }
            }
        }
        values.removeAll(matches);
    }

}
