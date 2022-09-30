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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TeeHttpServletResponseTest {

    public static Stream<Arguments> inputValues() {
        return Stream.of(
                Arguments.of( "utf-8", "G\u00FClc\u00FC",
                        new byte[] { (byte) 0x47, (byte) 0xC3, (byte) 0xBC, (byte) 0x6C, (byte) 0x63, (byte) 0xC3,
                                (byte) 0xBC}),
                Arguments.of("iso-8859-1", "G\u00FClc\u00FC",
                        new byte[] { (byte) 0x47, (byte) 0xFC, (byte) 0x6C, (byte) 0x63, (byte) 0xFC}));
    }

    @ParameterizedTest
    @MethodSource("inputValues")
    public void testWriterEncoding(String characterEncoding, String testString, byte[] expectedBytes) throws IOException {
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
