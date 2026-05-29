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
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.ClassicConstants;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StackTraceElementProxyTest {

    @Test
    public void nullStackTraceElementIsReplacedByNASubstitute() {
        StackTraceElementProxy step = new StackTraceElementProxy(null);

        assertNotNull(step.getStackTraceElement());
        assertSame(StackTraceElementProxy.NA_SUBSTITUTE, step.getStackTraceElement());

        StackTraceElement ste = step.getStackTraceElement();
        assertEquals(ClassicConstants.DECLARING_CLASS_NA, ste.getClassName());
        assertEquals(ClassicConstants.METHOD_NAME_NA, ste.getMethodName());
        assertEquals(ClassicConstants.FILENAME_NA, ste.getFileName());
        assertEquals(ClassicConstants.LINE_NUMBER_NA, ste.getLineNumber());
    }

    @Test
    public void getSTEAsStringWorksWithNullInput() {
        StackTraceElementProxy step = new StackTraceElementProxy(null);
        String steAsString = step.getSTEAsString();
        assertNotNull(steAsString);
        assertTrue(steAsString.startsWith("at "));
        assertTrue(steAsString.contains(ClassicConstants.DECLARING_CLASS_NA));
        assertTrue(steAsString.contains(ClassicConstants.METHOD_NAME_NA));
    }

    @Test
    public void toStringWorksWithNullInput() {
        StackTraceElementProxy step = new StackTraceElementProxy(null);
        assertEquals(step.getSTEAsString(), step.toString());
    }

    @Test
    public void hashCodeAndEqualsWorkWithNullInput() {
        StackTraceElementProxy step1 = new StackTraceElementProxy(null);
        StackTraceElementProxy step2 = new StackTraceElementProxy(null);

        assertEquals(step1, step2);
        assertEquals(step1.hashCode(), step2.hashCode());

        StackTraceElement realSTE = new StackTraceElement("com.example.Foo", "bar", "Foo.java", 42);
        StackTraceElementProxy step3 = new StackTraceElementProxy(realSTE);

        assertNotEquals(step1, step3);
        assertNotEquals(step1.hashCode(), step3.hashCode());
    }

    @Test
    public void equalsHandlesNullAndDifferentClass() {
        StackTraceElementProxy step = new StackTraceElementProxy(null);
        assertNotEquals(null, step);
        assertEquals(step, step);
    }

    @Test
    public void normalNonNullStackTraceElement() {
        StackTraceElement realSTE = new StackTraceElement("com.example.MyClass", "doSomething", "MyClass.java", 123);
        StackTraceElementProxy step = new StackTraceElementProxy(realSTE);

        assertSame(realSTE, step.getStackTraceElement());
        assertTrue(step.getSTEAsString().startsWith("at com.example.MyClass.doSomething(MyClass.java:123)"));
        assertEquals(step.getSTEAsString(), step.toString());
    }

    @Test
    public void classPackagingData() {
        StackTraceElementProxy step = new StackTraceElementProxy(null);

        assertEquals(null, step.getClassPackagingData());

        ClassPackagingData cpd = new ClassPackagingData("some.jar", "1.0", true);
        step.setClassPackagingData(cpd);
        assertSame(cpd, step.getClassPackagingData());

        // second set must throw
        assertThrows(IllegalStateException.class, () -> step.setClassPackagingData(new ClassPackagingData("x", "y")));

        // packaging data affects equals
        StackTraceElementProxy step2 = new StackTraceElementProxy(null);
        step2.setClassPackagingData(new ClassPackagingData("some.jar", "1.0", true));
        assertEquals(step, step2);

        StackTraceElementProxy step3 = new StackTraceElementProxy(null);
        step3.setClassPackagingData(new ClassPackagingData("other.jar", "2.0", false));
        assertNotEquals(step, step3);
    }

    @Test
    public void serializationRoundTripPreservesBehavior() throws IOException, ClassNotFoundException {
        // normal STE case
        StackTraceElement realSTE = new StackTraceElement("p.q.R", "m", "R.java", 7);
        StackTraceElementProxy original = new StackTraceElementProxy(realSTE);
        original.setClassPackagingData(new ClassPackagingData("loc", "ver"));
        String originalAsString = original.getSTEAsString(); // force cache of transient

        StackTraceElementProxy deserialized = roundTripSerialize(original);
        assertEquals(original, deserialized);
        assertEquals(originalAsString, deserialized.getSTEAsString());
        assertEquals(original.getClassPackagingData(), deserialized.getClassPackagingData()); // deserialized instance, use equals not same

        // null STE (NA_SUBSTITUTE) case
        StackTraceElementProxy originalNull = new StackTraceElementProxy(null);
        String originalNullAsString = originalNull.getSTEAsString();

        StackTraceElementProxy deserializedNull = roundTripSerialize(originalNull);
        assertEquals(originalNull, deserializedNull);
        assertEquals(StackTraceElementProxy.NA_SUBSTITUTE, deserializedNull.getStackTraceElement());
        assertEquals(originalNullAsString, deserializedNull.getSTEAsString());
    }

    private StackTraceElementProxy roundTripSerialize(StackTraceElementProxy input) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(input);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (StackTraceElementProxy) ois.readObject();
        }
    }
}
