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
package ch.qos.logback.core.joran.spi;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoAutoStartUtilTest {

    @Test
    public void commonObject() {
        Object o = new Object();
        assertTrue(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
    }

    @Test
    public void markedWithNoAutoStart() {
        DoNotAutoStart o = new DoNotAutoStart();
        assertFalse(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
    }
    

    
    /*
     * Annotation declared on implemented interface
     */
    @Test
    public void noAutoStartOnInterface() {
    	ComponentWithNoAutoStartOnInterface o = new ComponentWithNoAutoStartOnInterface();
        assertFalse(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
    }

    @NoAutoStart
    public interface NoAutoStartInterface {
    }
    
    private static class ComponentWithNoAutoStartOnInterface implements NoAutoStartInterface {
    }

    
    
    /*
     * Annotation declared on ancestor
     */
    @Test
    public void noAutoStartOnAncestor() {
    	ComponentWithNoAutoStartOnAncestor o = new ComponentWithNoAutoStartOnAncestor();
        assertFalse(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
    }
    
    private static class ComponentWithNoAutoStartOnAncestor extends DoNotAutoStart {	
    }

    
    
    /*
     * Annotation declared on interface implemented by an ancestor
     */
    @Test
    public void noAutoStartOnInterfaceImplementedByAncestor() {
    	ComponentWithAncestorImplementingInterfaceWithNoAutoStart o = new ComponentWithAncestorImplementingInterfaceWithNoAutoStart();
        assertFalse(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
    }
    
    private static class ComponentWithAncestorImplementingInterfaceWithNoAutoStart extends ComponentWithNoAutoStartOnInterface {	
    }

}
