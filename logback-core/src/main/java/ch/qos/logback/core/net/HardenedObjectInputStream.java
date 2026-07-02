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
package ch.qos.logback.core.net;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * HardenedObjectInputStream restricts the set of classes that can be
 * deserialized to a set of explicitly whitelisted classes. This prevents
 * certain type of attacks from being successful.
 * 
 * <p>
 * It is assumed that classes in the "java.lang" and "java.util" packages are
 * always authorized.
 * </p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2.0
 */
public class HardenedObjectInputStream extends ObjectInputStream {

    final private List<String> whitelistedClassNames;
    final private static String[] JAVA_CLASSES = new String[] { "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Number",
            "java.lang.Short",
            "java.lang.String",
            "java.lang.Throwable",
            "java.util.ArrayList",
            "java.util.Collections$EmptyMap",
            "java.util.Collections$UnmodifiableMap",
            "java.util.concurrent.CopyOnWriteArrayList",
            "java.util.HashMap"
            //"java.util.HashSet",
            //"java.util.Hashtable",

            // PASS
            //"java.util.LinkedHashMap",
            //"java.util.LinkedHashSet",
            //"java.util.LinkedList",
            //"java.util.Stack",
            //"java.util.TreeMap",
            //"java.util.TreeSet",
            //"java.util.Vector"
    };
    final private static int DEPTH_LIMIT = 16;
    final private static int ARRAY_LIMIT = 10000;
    final private static int ERROR_COUNT_LIMIT = 10;

    final private ContextAwareImpl contextAware;
    final private HashMap<String, Integer> errorMap = new HashMap<>();

    public HardenedObjectInputStream(Context context, InputStream in, String[] whitelistStrings) throws IOException {
      this(context, in, Arrays.asList(whitelistStrings));
    }
    public HardenedObjectInputStream(Context context, InputStream in, List<String> whitelist) throws IOException {
        super(in);

        if(context != null)
            this.contextAware = new ContextAwareImpl(context, this);
         else
            this.contextAware = null;

        this.initObjectFilter();
        this.whitelistedClassNames = new ArrayList<String>();
        this.whitelistedClassNames.addAll(whitelist);
    }


    private void initObjectFilter() {
        this.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                "maxarray=" + ARRAY_LIMIT + ";maxdepth=" + DEPTH_LIMIT + ";"
        ));
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass anObjectStreamClass) throws IOException, ClassNotFoundException {

        String incomingClassName = anObjectStreamClass.getName();

        if (!isWhitelisted(incomingClassName)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", anObjectStreamClass.getName());
        }

        return super.resolveClass(anObjectStreamClass);
    }

    /**
     * There is no reason to have proxy classes in logback deserialization, so we just
     * throw an exception here to prevent any potential bypasses that could be achieved
     * through proxy classes.
     *
     * @param interfaces the list of interface names that were
     *                deserialized in the proxy class descriptor
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @since 1.5.34
     */
    @Override
    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        throw new InvalidClassException("Unauthorized deserialization attempt ", Arrays.toString(interfaces));
    }

    private boolean isWhitelisted(String incomingClassName) {
        for (String javaClass : JAVA_CLASSES) {
            if (incomingClassName.equals(javaClass))
                return true;
        }
        for (String whiteListed : whitelistedClassNames) {
            if (incomingClassName.equals(whiteListed))
                return true;
        }


        int errorCount =   errorMap.getOrDefault(incomingClassName, 0) + 1;
        errorMap.put(incomingClassName, errorCount);
        if(contextAware != null && errorCount < ERROR_COUNT_LIMIT) {
            contextAware.addError("Unauthorized deserialization attempt for class [" + incomingClassName+"]");
            contextAware.addError(("If you deem the class to be legitimate, please contact the project maintainers to have it whitelisted."));
        }

        return false;
    }

    protected void addToWhitelist(List<String> additionalAuthorizedClasses) {
        whitelistedClassNames.addAll(additionalAuthorizedClasses);
    }
}
