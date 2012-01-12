/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 2012, VMware Inc. All rights reserved.
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
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.StatusManager;
import org.codehaus.janino.SimpleCompiler;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class JaninoEventEvaluatorBaseTest {

    JaninoEventEvaluatorBase jee = new JaninoEventEvaluatorBase() {
        @Override
        protected String getDecoratedExpression() {
            return "return true;";
        }

        @Override
        protected String[] getParameterNames() {
            return new String[0];
        }

        @Override
        protected Class[] getParameterTypes() {
            return new Class[0];
        }

        @Override
        protected Object[] getParameterValues(Object event) {
            return new Object[0];
        }
    };

    Context context = new Context() {
        public StatusManager getStatusManager() {
            return null;
        }

        public Object getObject(String key) {
            return null;
        }

        public void putObject(String key, Object value) {
        }

        public String getProperty(String key) {
            return null;
        }

        public void putProperty(String key, String value) {
        }

        public Map<String, String> getCopyOfPropertyMap() {
            return null;
        }

        public String getName() {
            return null;
        }

        public void setName(String name) {
        }

        public long getBirthTime() {
            return 0;
        }

        public Object getConfigurationLock() {
            return null;
        }

        public ExecutorService getExecutorService() {
            return null;
        }
    };

    public JaninoEventEvaluatorBaseTest() {
        jee.setContext(context);
    }

    // See LBCORE-244
    @Test
    public void testJaninoParentClassLoader() throws Exception {
        Thread.currentThread().setContextClassLoader(null);
        jee.start();

        Field parentClassLoaderField = SimpleCompiler.class.getDeclaredField("parentClassLoader");
        parentClassLoaderField.setAccessible(true);
        ClassLoader parentClassLoader = (ClassLoader) parentClassLoaderField.get(jee.scriptEvaluator);

        assertEquals(context.getClass().getClassLoader(), parentClassLoader);
    }

}
