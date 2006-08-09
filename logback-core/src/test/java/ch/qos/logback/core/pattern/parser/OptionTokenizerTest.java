/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.pattern.parser.OptionTokenizer;
import ch.qos.logback.core.pattern.parser.ScanException;

import junit.framework.TestCase;

public class OptionTokenizerTest extends TestCase {

	public void testEmpty() throws ScanException {
		{
			List ol = new OptionTokenizer("").tokenize();
			List witness = new ArrayList();
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer(" ").tokenize();
			List witness = new ArrayList();
			assertEquals(witness, ol);
		}
	}

	
	public void testSimple() throws ScanException {
		{
			List ol = new OptionTokenizer("abc").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add("abc");
			assertEquals(witness, ol);
		}
	}
	
	public void testSingleQuote() throws ScanException {
		{
			List ol = new OptionTokenizer("' '").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" ");
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer("' x\t'").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" x\t");
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer("' x\\t'").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" x\t");
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer("' x\\''").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" x\'");
			assertEquals(witness, ol);
		}
	}
	
	public void testDoubleQuote() throws ScanException {
		{
			List ol = new OptionTokenizer("\" \"").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" ");
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer("\" x\t\"").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" x\t");
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer("\" x\\t\"").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" x\t");
			assertEquals(witness, ol);
		}
		
		{
			List ol = new OptionTokenizer("\" x\\\"\"").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add(" x\"");
			assertEquals(witness, ol);
		}
	}
	
	public void testMultiple() throws ScanException {
		{
			List ol = new OptionTokenizer("a, b").tokenize();
			List<String> witness = new ArrayList<String>();
			witness.add("a");
			witness.add("b");
			assertEquals(witness, ol);
		}
	}

}