/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

/**
 * @author Ceki
 * 
 */
public class FileNamePatternTest {

  Context context = new ContextBase();

  @Test
  public void testSmoke() {
    // System.out.println("Testing [t]");
    FileNamePattern pp = new FileNamePattern("t", context);
    assertEquals("t", pp.convertInt(3));

    // System.out.println("Testing [foo]");
    pp = new FileNamePattern("foo", context);
    assertEquals("foo", pp.convertInt(3));

    // System.out.println("Testing [foo%]");
    // pp = new FileNamePattern("foo%", context);
    // StatusPrinter.print(context.getStatusManager());
    // assertEquals("foo%", pp.convertInt(3));

    pp = new FileNamePattern("%i foo", context);
    
    assertEquals("3 foo", pp.convertInt(3));

    pp = new FileNamePattern("foo%i.xixo", context);
    assertEquals("foo3.xixo", pp.convertInt(3));

    pp = new FileNamePattern("foo%i.log", context);
    assertEquals("foo3.log", pp.convertInt(3));

    pp = new FileNamePattern("foo.%i.log", context);
    assertEquals("foo.3.log", pp.convertInt(3));

    pp = new FileNamePattern("%i.foo\\%", context);
    assertEquals("3.foo%", pp.convertInt(3));

    pp = new FileNamePattern("\\%foo", context);
    assertEquals("%foo", pp.convertInt(3));
  }

  @Test
  // test ways for dealing with flowing i converter, as in "foo%ix"
  public void flowingI() {
    {
      FileNamePattern pp = new FileNamePattern("foo%i{}bar%i", context);
      assertEquals("foo3bar3", pp.convertInt(3));
    }
    {
      FileNamePattern pp = new FileNamePattern("foo%i{}bar%i", context);
      assertEquals("foo3bar3", pp.convertInt(3));
    }
  }

  @Test
  public void date() {
    Calendar cal = Calendar.getInstance();
    cal.set(2003, 4, 20, 17, 55);

    FileNamePattern pp = new FileNamePattern("foo%d{yyyy.MM.dd}", context);
    
    assertEquals("foo2003.05.20", pp.convert(cal.getTime()));

    pp = new FileNamePattern("foo%d{yyyy.MM.dd HH:mm}", context);
    assertEquals("foo2003.05.20 17:55", pp.convert(cal.getTime()));

    pp = new FileNamePattern("%d{yyyy.MM.dd HH:mm} foo", context);
    assertEquals("2003.05.20 17:55 foo", pp.convert(cal.getTime()));

  }

  @Test
  public void withBackslash() {
    FileNamePattern pp = new FileNamePattern("c:\\foo\\bar.%i", context);
    assertEquals("c:\\foo\\bar.3", pp.convertInt(3));
  }


  @Test
  public void objectListConverter() {
    Calendar cal = Calendar.getInstance();
    cal.set(2003, 4, 20, 17, 55);
    FileNamePattern fnp = new FileNamePattern("foo-%d{yyyy.MM.dd}-%i.txt", context);
    assertEquals("foo-2003.05.20-79.txt", fnp.convertMultipleArguments(cal.getTime(), 79));
  }

  @Test
  public void asRegexByDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(2003, 4, 20, 17, 55);
    
    FileNamePattern fnp = new FileNamePattern("foo-%d{yyyy.MM.dd}-%i.txt", context);
    
    String regex = fnp.asRegex(cal.getTime());
    
    assertEquals("foo-2003.05.20-\\d{1,2}.txt", regex);
  }
  

}
