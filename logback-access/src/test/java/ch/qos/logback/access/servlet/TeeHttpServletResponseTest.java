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
package ch.qos.logback.access.servlet;

import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServletOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

@RunWith(Parameterized.class)
public class TeeHttpServletResponseTest {

    String characterEncoding;
    String testString;
    byte[] expectedBytes; 

    public TeeHttpServletResponseTest(String characterEncoding, String testString, byte[] expectedBytes) {
        this.characterEncoding = characterEncoding;
        this.testString = testString;
        this.expectedBytes = expectedBytes;
    }

    @Parameterized.Parameters
    public static Collection<?> inputValues() {
        return Arrays.asList(new Object[][] {
                { "utf-8", "G\u00FClc\u00FC", new byte[] { (byte) 0x47, (byte) 0xC3, (byte) 0xBC, (byte) 0x6C, (byte) 0x63, (byte) 0xC3, (byte) 0xBC } },
                { "iso-8859-1", "G\u00FClc\u00FC", new byte[] { (byte) 0x47, (byte) 0xFC, (byte) 0x6C, (byte) 0x63, (byte) 0xFC } } });
    }

    @Test
    public void testWriterEncoding() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        DummyResponse dummyResponse = new DummyResponse();
        dummyResponse.setCharacterEncoding(characterEncoding);
        dummyResponse.setOutputStream(new DummyServletOutputStream(byteArrayOutputStream));

        TeeHttpServletResponse teeServletResponse = new TeeHttpServletResponse(dummyResponse);

        PrintWriter writer = teeServletResponse.getWriter();
        writer.write(testString);
        writer.flush();

        assertArrayEquals(expectedBytes, byteArrayOutputStream.toByteArray());
    }

}
