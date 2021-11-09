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
package ch.qos.logback.core.pattern.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ch.qos.logback.core.pattern.FormatInfo;

public class FormatInfoTest {

    @Test
    public void testEndingInDot() {
        try {
            FormatInfo.valueOf("45.");
            fail("45. is not a valid format info string");
        } catch (final IllegalArgumentException iae) {
            // OK
        }
    }

    @Test
    public void testBasic() {
        {
            final FormatInfo fi = FormatInfo.valueOf("45");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(45);
            assertEquals(witness, fi);
        }

        {
            final FormatInfo fi = FormatInfo.valueOf("4.5");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(4);
            witness.setMax(5);
            assertEquals(witness, fi);
        }
    }

    @Test
    public void testRightPad() {
        {
            final FormatInfo fi = FormatInfo.valueOf("-40");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(40);
            witness.setLeftPad(false);
            assertEquals(witness, fi);
        }

        {
            final FormatInfo fi = FormatInfo.valueOf("-12.5");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(12);
            witness.setMax(5);
            witness.setLeftPad(false);
            assertEquals(witness, fi);
        }

        {
            final FormatInfo fi = FormatInfo.valueOf("-14.-5");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(14);
            witness.setMax(5);
            witness.setLeftPad(false);
            witness.setLeftTruncate(false);
            assertEquals(witness, fi);
        }
    }

    @Test
    public void testMinOnly() {
        {
            final FormatInfo fi = FormatInfo.valueOf("49");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(49);
            assertEquals(witness, fi);
        }

        {
            final FormatInfo fi = FormatInfo.valueOf("-587");
            final FormatInfo witness = new FormatInfo();
            witness.setMin(587);
            witness.setLeftPad(false);
            assertEquals(witness, fi);
        }

    }

    @Test
    public void testMaxOnly() {
        {
            final FormatInfo fi = FormatInfo.valueOf(".49");
            final FormatInfo witness = new FormatInfo();
            witness.setMax(49);
            assertEquals(witness, fi);
        }

        {
            final FormatInfo fi = FormatInfo.valueOf(".-5");
            final FormatInfo witness = new FormatInfo();
            witness.setMax(5);
            witness.setLeftTruncate(false);
            assertEquals(witness, fi);
        }
    }
}