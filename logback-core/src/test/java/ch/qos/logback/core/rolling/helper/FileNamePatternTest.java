/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
    FileNamePattern pp = new FileNamePattern("t", context);
    assertEquals("t", pp.convertInt(3));

    pp = new FileNamePattern("foo", context);
    assertEquals("foo", pp.convertInt(3));

    pp = new FileNamePattern("%i foo", context);

    assertEquals("3 foo", pp.convertInt(3));

    pp = new FileNamePattern("foo%i.xixo", context);
    assertEquals("foo3.xixo", pp.convertInt(3));

    pp = new FileNamePattern("foo%i.log", context);
    assertEquals("foo3.log", pp.convertInt(3));

    pp = new FileNamePattern("foo.%i.log", context);
    assertEquals("foo.3.log", pp.convertInt(3));

    //pp = new FileNamePattern("%i.foo\\%", context);
    //assertEquals("3.foo%", pp.convertInt(3));

    //pp = new FileNamePattern("\\%foo", context);
    //assertEquals("%foo", pp.convertInt(3));
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
    assertEquals("c:/foo/bar.3", pp.convertInt(3));
  }

  @Test
  public void objectListConverter() {
    Calendar cal = Calendar.getInstance();
    cal.set(2003, 4, 20, 17, 55);
    FileNamePattern fnp = new FileNamePattern("foo-%d{yyyy.MM.dd}-%i.txt",
        context);
    assertEquals("foo-2003.05.20-79.txt", fnp.convertMultipleArguments(cal
        .getTime(), 79));
  }

  @Test
  public void asRegexByDate() {

    Calendar cal = Calendar.getInstance();
    cal.set(2003, 4, 20, 17, 55);

    {
      FileNamePattern fnp = new FileNamePattern("foo-%d{yyyy.MM.dd}-%i.txt",
          context);
      String regex = fnp.toRegex(cal.getTime());
      assertEquals("foo-2003.05.20-(\\d{1,2}).txt", regex);
    }
    {
      FileNamePattern fnp = new FileNamePattern("\\toto\\foo-%d{yyyy\\MM\\dd}-%i.txt",
          context);
      String regex = fnp.toRegex(cal.getTime());
      assertEquals("/toto/foo-2003/05/20-(\\d{1,2}).txt", regex);
    }
  }

  @Test
  public void asRegex() {
    {
      FileNamePattern fnp = new FileNamePattern("foo-%d{yyyy.MM.dd}-%i.txt",
          context);
      String regex = fnp.toRegex();
      assertEquals("foo-\\d{4}\\.\\d{2}\\.\\d{2}-\\d{1,2}.txt", regex);
    }
    {
      FileNamePattern fnp = new FileNamePattern("foo-%d{yyyy.MM.dd'T'}-%i.txt",
          context);
      String regex = fnp.toRegex();
      assertEquals("foo-\\d{4}\\.\\d{2}\\.\\d{2}T-\\d{1,2}.txt", regex);
    }
  }
}
