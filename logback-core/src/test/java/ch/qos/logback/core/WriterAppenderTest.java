/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core;


import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.pattern.parser.SamplePatternLayout;

public class WriterAppenderTest {

  Context context = new ContextBase();
  
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void smoke() {
    String FILE_HEADER = "FILE_HEADER ";
    String PRESENTATION_HEADER = "PRESENTATION_HEADER";
    String PRESENTATION_FOOTER = "PRESENTATION_FOOTER ";
    String FILE_FOOTER = "FILE_FOOTER";
    headerFooterCheck(FILE_HEADER, PRESENTATION_HEADER, PRESENTATION_FOOTER, FILE_FOOTER);
  }

  @Test
  public void nullFileHeader() {
    String FILE_HEADER = null;
    String PRESENTATION_HEADER = "PRESENTATION_HEADER";
    String PRESENTATION_FOOTER = "PRESENTATION_FOOTER ";
    String FILE_FOOTER = "FILE_FOOTER";
    headerFooterCheck(FILE_HEADER, PRESENTATION_HEADER, PRESENTATION_FOOTER, FILE_FOOTER);
  }

  @Test
  public void nullPresentationHeader() {
    String FILE_HEADER = "FILE_HEADER ";
    String PRESENTATION_HEADER = null;
    String PRESENTATION_FOOTER = "PRESENTATION_FOOTER ";
    String FILE_FOOTER = "FILE_FOOTER";
    headerFooterCheck(FILE_HEADER, PRESENTATION_HEADER, PRESENTATION_FOOTER, FILE_FOOTER);
  }

  @Test
  public void nullPresentationFooter() {
    String FILE_HEADER = "FILE_HEADER ";
    String PRESENTATION_HEADER =  "PRESENTATION_HEADER";
    String PRESENTATION_FOOTER = null;
    String FILE_FOOTER = "FILE_FOOTER";
    headerFooterCheck(FILE_HEADER, PRESENTATION_HEADER, PRESENTATION_FOOTER, FILE_FOOTER);
  }
  
  @Test
  public void nullFileFooter() {
    String FILE_HEADER = "FILE_HEADER ";
    String PRESENTATION_HEADER = "PRESENTATION_HEADER";
    String PRESENTATION_FOOTER = "PRESENTATION_FOOTER ";
    String FILE_FOOTER = null;
    headerFooterCheck(FILE_HEADER, PRESENTATION_HEADER, PRESENTATION_FOOTER, FILE_FOOTER);
  }
  
  public void headerFooterCheck(String fileHeader, String presentationHeader, String presentationFooter, String fileFooter) {
    WriterAppender<Object> wa = new WriterAppender<Object>();
    wa.setContext(context);
    Writer sw = new StringWriter();
 
    SamplePatternLayout<Object> spl = new SamplePatternLayout<Object>();
    spl.setContext(context);
  
    spl.setFileHeader(fileHeader);
    spl.setPresentationHeader(presentationHeader);
    spl.setPresentationFooter(presentationFooter);
    spl.setFileFooter(fileFooter);
  
    spl.start();
    
    wa.setLayout(spl);
    wa.setWriter(sw);
    wa.start();
    
    wa.stop();
    String result = sw.toString();

    String expectedHeader = emtptyIfNull(fileHeader) + emtptyIfNull(presentationHeader);
    assertTrue(result, result.startsWith(expectedHeader));

    String expectedFooter = emtptyIfNull(presentationFooter) + emtptyIfNull(fileFooter);
    assertTrue(result, result.endsWith(expectedFooter));
  }
  
  String emtptyIfNull(String s) {
    return s == null ? "" : s;
  }
}
