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
package ch.qos.logback.core.joran.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.ext.IncAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class IncludeActionTest {

  final static String INCLUDE_KEY = "includeKey";
  final static String SUB_FILE_KEY = "subFileKey";
  final static String SECOND_FILE_KEY = "secondFileKey";

  Context context = new ContextBase();
  TrivialConfigurator tc;

  static final String INCLUSION_DIR_PREFIX = "src/test/input/joran/inclusion/";

  static final String TOP_BY_FILE = INCLUSION_DIR_PREFIX + "topByFile.xml";

  static final String SUB_FILE = INCLUSION_DIR_PREFIX + "subByFile.xml";

  static final String MULTI_INCLUDE_BY_FILE = INCLUSION_DIR_PREFIX
      + "multiIncludeByFile.xml";

  static final String SECOND_FILE = INCLUSION_DIR_PREFIX + "second.xml";

  static final String TOP_BY_URL = INCLUSION_DIR_PREFIX + "topByUrl.xml";

  static final String INCLUDE_BY_RESOURCE = INCLUSION_DIR_PREFIX
      + "topByResource.xml";

  static final String INCLUDED_FILE = INCLUSION_DIR_PREFIX + "included.xml";
  static final String URL_TO_INCLUDE = "file:./" + INCLUDED_FILE;

  static final String INVALID = INCLUSION_DIR_PREFIX + "invalid.xml";

  static final String INCLUDED_AS_RESOURCE = "asResource/joran/inclusion/includedAsResource.xml";

  int diff = RandomUtil.getPositiveInt();
  
  public IncludeActionTest() {
    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("x"), new NOPAction());
    rulesMap.put(new Pattern("x/inc"), new IncAction());
    rulesMap.put(new Pattern("x/include"), new IncludeAction());

    tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
  }

  @Before
  public void setUp() throws Exception {
    IncAction.reset();
  }

  @After
  public void tearDown() throws Exception {
    context = null;
    System.clearProperty(INCLUDE_KEY);
    System.clearProperty(SECOND_FILE_KEY);
    System.clearProperty(SUB_FILE_KEY);
  }

  @Test
  public void basicFile() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_FILE);
    tc.doConfigure(TOP_BY_FILE);
    verifyConfig(2);
  }

  @Test
  public void basicResource() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_AS_RESOURCE);
    tc.doConfigure(INCLUDE_BY_RESOURCE);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  @Test
  public void basicURL() throws JoranException {
    System.setProperty(INCLUDE_KEY, URL_TO_INCLUDE);
    tc.doConfigure(TOP_BY_URL);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  @Test
  public void noFileFound() throws JoranException {
    System.setProperty(INCLUDE_KEY, "toto");
    tc.doConfigure(TOP_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(FileNotFoundException.class));
  }

  @Test
  public void withCorruptFile() throws JoranException, IOException {
    String tmpOut = copyToTemp(INVALID);
    System.setProperty(INCLUDE_KEY, tmpOut);
    tc.doConfigure(TOP_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(SAXParseException.class));

    // we like to erase the temp file in order to see
    // if http://jira.qos.ch/browse/LBCORE-122 was fixed
    File f = new File(tmpOut);
    assertTrue(f.exists());
    assertTrue(f.delete());
    
  }

  String copyToTemp(String in)  throws IOException {
    FileInputStream fis = new FileInputStream(in);
    String out = CoreTestConstants.OUTPUT_DIR_PREFIX+"out"+diff;
    FileOutputStream fos = new FileOutputStream(out);
    int b;
    while((b=fis.read()) != -1) {
      fos.write(b);
    }
    fis.close();
    fos.close();
    return out;
  }
  
  @Test
  public void malformedURL() throws JoranException {
    System.setProperty(INCLUDE_KEY, "htp://logback.qos.ch");
    tc.doConfigure(TOP_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(MalformedURLException.class));
  }

  @Test
  public void unknownURL() throws JoranException {
    System.setProperty(INCLUDE_KEY, "http://logback2345.qos.ch");
    tc.doConfigure(TOP_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(UnknownHostException.class));
  }

  @Test
  public void nestedInclude() throws JoranException {
    System.setProperty(SUB_FILE_KEY, INCLUDED_FILE);
    System.setProperty(INCLUDE_KEY, SECOND_FILE);
    tc.doConfigure(TOP_BY_FILE);
    StatusPrinter.print(context);
    verifyConfig(1);

  }

  @Test
  public void multiInclude() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_FILE);
    System.setProperty(SECOND_FILE_KEY, SECOND_FILE);
    tc.doConfigure(MULTI_INCLUDE_BY_FILE);
    verifyConfig(3);
  }

  @Test
  public void saxParseException() throws JoranException {
    System.setProperty(INCLUDE_KEY, INCLUDED_FILE);
    System.setProperty(SECOND_FILE_KEY, SECOND_FILE);
    tc.doConfigure(MULTI_INCLUDE_BY_FILE);
    verifyConfig(3);
  }

  @Test
  public void errorInDoBegin() {

  }

  void verifyConfig(int expected) {
    assertEquals(expected, IncAction.beginCount);
    assertEquals(expected, IncAction.endCount);
  }
}
