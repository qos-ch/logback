/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class WriteSpeed {
  static final String MSG = "some xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxmedium length message sadf;-90123lklkhasdfl lkahjqdlsfh";

  static final ByteBuffer[] BB = new ByteBuffer[0];

  public static void main(String[] args) throws Exception {

    long size = 100 * 1000;
    nio(size);
    oldPattern(size);
  }


  static void oldPattern(long size) throws Exception {
    PatternLayout layout = new PatternLayout("%r [%t] %p %c - %m%n");
    FileAppender appender = new FileAppender(layout, "toto.log", false);

    Logger logger = LogManager.getRootLogger();
    logger.removeAllAppenders();
    logger.addAppender(appender);

    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      logger.info(MSG);
    }
    long result = System.nanoTime() - start;
    System.out.println("Average OLD write speed: " + (result / size) + " nanos");
  }

  static void nio(long size) throws Exception {

    //Charset cs = Charset.forName("ISO-8859-1");
    //Charset cs = Charset.forName("US-ASCII");
    //CharsetEncoder encoder = cs.newEncoder();
    //String str = "1081 [main] INFO root -" + MSG + "\r\n";


    //CharBuffer cbuf = CharBuffer.wrap(str);
    //ByteBuffer buf = ByteBuffer.wrap(str.getBytes());
    FileOutputStream fos = new FileOutputStream("niototo.log", false);
    FileChannel channel = fos.getChannel();

    //CoderResult cr;
    //long s = size*(cbuf.length())/(8*1024);
    ByteBuffer[] BALL = new ByteBuffer[] {all};
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {

      channel.write(BALL);
      all.rewind();
//      for (int j = 0; j < al.size(); j++) {
//        ByteBuffer bb = (ByteBuffer) al.get(j);
//        bb.rewind();
//      }
    }
    //buf.flip();
    //channel.write(buf);

    long result = System.nanoTime() - start;
    System.out.println("NIO Average write speed: " + (result / size) + " nanos");


    channel.close();


  }

  static ByteBuffer x1 = ByteBuffer.wrap(" [".getBytes());
  static ByteBuffer x2 = ByteBuffer.wrap("] ".getBytes());
  static ByteBuffer x3 = ByteBuffer.wrap("INFO".getBytes());
  static ByteBuffer x4 = ByteBuffer.wrap(" ".getBytes());
  static ByteBuffer x5 = ByteBuffer.wrap(" -".getBytes());

  static ArrayList bbList = new ArrayList();

  static ByteBuffer z1 = ByteBuffer.wrap("1081".getBytes());
  static ByteBuffer z2 = ByteBuffer.wrap("main".getBytes());
  static ByteBuffer z3 = ByteBuffer.wrap(MSG.getBytes());
  static ByteBuffer z4 = ByteBuffer.wrap("\r\n".getBytes());

  static String s = "1081 [main] INFO root - " + MSG + "\r\n";
  static ByteBuffer all = ByteBuffer.wrap(s.getBytes());

  static ArrayList makeList() {

    //  "1081 [main] INFO root -" + MSG + "\r\n";
    bbList.clear();
    bbList.add(all);
//    bbList.add(z1);
//    bbList.add(x1);
//    bbList.add(z2);
//    bbList.add(x2);
//    bbList.add(x3);
//    bbList.add(x4);
//    bbList.add(x5);
//    bbList.add(z3);
//    bbList.add(z4);
    return bbList;

  }
}
