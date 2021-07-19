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
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ch.qos.logback.core.net.AutoFlushingObjectWriter}.
 *
 * @author Sebastian Gr&ouml;bler
 */
@Ignore
public class AutoFlushingObjectWriterTest {

    private InstrumentedObjectOutputStream objectOutputStream;

    @Before
    public void beforeEachTest() throws IOException {
        objectOutputStream = spy(new InstrumentedObjectOutputStream());
    }

    @Test
    public void writesToUnderlyingObjectOutputStream() throws IOException {

        // given
        ObjectWriter objectWriter = new AutoFlushingObjectWriter(objectOutputStream, 2);
        String object = "foo";

        // when
        objectWriter.write(object);

        // then
        verify(objectOutputStream).writeObjectOverride(object);
    }

    @Test
    public void flushesAfterWrite() throws IOException {

        // given
        ObjectWriter objectWriter = new AutoFlushingObjectWriter(objectOutputStream, 2);
        String object = "foo";

        // when
        objectWriter.write(object);

        // then
        InOrder inOrder = inOrder(objectOutputStream);
        inOrder.verify(objectOutputStream).writeObjectOverride(object);
        inOrder.verify(objectOutputStream).flush();
    }

    @Test
    public void resetsObjectOutputStreamAccordingToGivenResetFrequency() throws IOException {

        // given
        ObjectWriter objectWriter = new AutoFlushingObjectWriter(objectOutputStream, 2);
        String object = "foo";

        // when
        objectWriter.write(object);
        objectWriter.write(object);
        objectWriter.write(object);
        objectWriter.write(object);

        // then
        InOrder inOrder = inOrder(objectOutputStream);
        inOrder.verify(objectOutputStream).writeObjectOverride(object);
        inOrder.verify(objectOutputStream).writeObjectOverride(object);
        inOrder.verify(objectOutputStream).reset();
        inOrder.verify(objectOutputStream).writeObjectOverride(object);
        inOrder.verify(objectOutputStream).writeObjectOverride(object);
        inOrder.verify(objectOutputStream).reset();
    }

    private static class InstrumentedObjectOutputStream extends ObjectOutputStream {

        protected InstrumentedObjectOutputStream() throws IOException, SecurityException {
        	super();
        }

        @Override
        protected void writeObjectOverride(final Object obj) throws IOException {
            // nop
        }

        @Override
        public void flush() throws IOException {
            // nop
        }

        @Override
        public void reset() throws IOException {
            // nop
        }
    }
}
