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

import org.junit.Test;

public class OptionTokenizerTest {

    @Test
    public void testEmpty() {

    }

    //
    // @Test
    // public void testEmpty() throws ScanException {
    // {
    // List ol = new OptionTokenizer("").tokenize();
    // List witness = new ArrayList();
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer(" ").tokenize();
    // List witness = new ArrayList();
    // assertEquals(witness, ol);
    // }
    // }
    //
    // @Test
    // public void testSimple() throws ScanException {
    // {
    // List ol = new OptionTokenizer("abc").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add("abc");
    // assertEquals(witness, ol);
    // }
    // }
    //
    // @Test
    // public void testSingleQuote() throws ScanException {
    // {
    // List ol = new OptionTokenizer("' '").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" ");
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer("' x\t'").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" x\t");
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer("' x\\t'").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" x\\t");
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer("' x\\''").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" x\\'");
    // assertEquals(witness, ol);
    // }
    // }
    //
    //
    //
    // @Test
    // public void testDoubleQuote() throws ScanException {
    // {
    // List ol = new OptionTokenizer("\" \"").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" ");
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer("\" x\t\"").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" x\t");
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer("\" x\\t\"").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" x\\t");
    // assertEquals(witness, ol);
    // }
    //
    // {
    // List ol = new OptionTokenizer("\" x\\\"\"").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add(" x\\\"");
    // assertEquals(witness, ol);
    // }
    // }
    //
    // @Test
    // public void testMultiple() throws ScanException {
    // {
    // List ol = new OptionTokenizer("a, b").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add("a");
    // witness.add("b");
    // assertEquals(witness, ol);
    // }
    // {
    // List ol = new OptionTokenizer("'a', b").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add("a");
    // witness.add("b");
    // assertEquals(witness, ol);
    // }
    // {
    // List ol = new OptionTokenizer("'', b").tokenize();
    // List<String> witness = new ArrayList<String>();
    // witness.add("");
    // witness.add("b");
    // assertEquals(witness, ol);
    // }
    // }
    //
}