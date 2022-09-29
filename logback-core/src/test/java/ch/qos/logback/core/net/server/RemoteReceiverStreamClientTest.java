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
package ch.qos.logback.core.net.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.net.mock.MockContext;

/**
 * Unit tests for {@link RemoteReceiverStreamClient}.
 *
 * @author Carl Harris
 */
public class RemoteReceiverStreamClientTest {

    private static final String TEST_EVENT = "test event";

    private MockContext context = new MockContext();

    private MockEventQueue queue = new MockEventQueue();

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private RemoteReceiverStreamClient client = new RemoteReceiverStreamClient("someId", outputStream);

    @BeforeEach
    public void setUp() throws Exception {
        client.setContext(context);
        client.setQueue(queue);
    }

    @Test
    public void testOfferEventAndRun() throws Exception {
        client.offer(TEST_EVENT);

        Thread thread = new Thread(client);
        thread.start();

        // MockEventQueue will interrupt the thread when the queue is drained
        thread.join(1000);
        Assertions.assertFalse(thread.isAlive());

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        Assertions.assertEquals(TEST_EVENT, ois.readObject());
    }

    @Test
    public void testOfferEventSequenceAndRun() throws Exception {
        for (int i = 0; i < 10; i++) {
            client.offer(TEST_EVENT + i);
        }

        Thread thread = new Thread(client);
        thread.start();
        thread.join(1000);
        Assertions.assertFalse(thread.isAlive());

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(TEST_EVENT + i, ois.readObject());
        }
    }

}
