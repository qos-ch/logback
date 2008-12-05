/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ch.qos.logback.core.pattern.FormatInfo;


public class FormatInfoTest  {

  @Test
  public void testEndingInDot() {
    try {
      FormatInfo.valueOf("45.");
      fail("45. is not a valid format info string");
    } catch (IllegalArgumentException iae) {
      // OK
    }
  }

  @Test
  public void testBasic() {
    {
      FormatInfo fi = FormatInfo.valueOf("45");
      FormatInfo witness = new FormatInfo();
      witness.setMin(45);
      assertEquals(witness, fi);
    }

    {
      FormatInfo fi = FormatInfo.valueOf("4.5");
      FormatInfo witness = new FormatInfo();
      witness.setMin(4);
      witness.setMax(5);
      assertEquals(witness, fi);
    }
  }

  @Test
  public void testRightPad() {
    {
      FormatInfo fi = FormatInfo.valueOf("-40");
      FormatInfo witness = new FormatInfo();
      witness.setMin(40);
      witness.setLeftPad(false);
      assertEquals(witness, fi);
    }

    {
      FormatInfo fi = FormatInfo.valueOf("-12.5");
      FormatInfo witness = new FormatInfo();
      witness.setMin(12);
      witness.setMax(5);
      witness.setLeftPad(false);
      assertEquals(witness, fi);
    }

    {
      FormatInfo fi = FormatInfo.valueOf("-14.-5");
      FormatInfo witness = new FormatInfo();
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
      FormatInfo fi = FormatInfo.valueOf("49");
      FormatInfo witness = new FormatInfo();
      witness.setMin(49);
      assertEquals(witness, fi);
    }

    {
      FormatInfo fi = FormatInfo.valueOf("-587");
      FormatInfo witness = new FormatInfo();
      witness.setMin(587);
      witness.setLeftPad(false);
      assertEquals(witness, fi);
    }

  }

  @Test
  public void testMaxOnly() {
    {
      FormatInfo fi = FormatInfo.valueOf(".49");
      FormatInfo witness = new FormatInfo();
      witness.setMax(49);
      assertEquals(witness, fi);
    }

    {
      FormatInfo fi = FormatInfo.valueOf(".-5");
      FormatInfo witness = new FormatInfo();
      witness.setMax(5);
      witness.setLeftTruncate(false);
      assertEquals(witness, fi);
    }
  }
}