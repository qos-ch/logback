/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.corpus;

import java.util.List;
import java.util.Random;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;

public class CorpusMaker {

  // level distribution is determined by the following table
  // it corresponds to TRACE 20%, DEBUG 30%, INFO 30%, WARN 10%,
  // ERROR 10%. See also getRandomLevel() method.
  static final double[] LEVEL_DISTRIBUTION = new double[] { .2, .5, .8, .9 };

  // messages will have no arguments 80% of the time, one argument in 8%, two
  // arguments in 7% and three arguments in 5% of cases
  static final double[] ARGUMENT_DISTRIBUTION = new double[] { .80, .88, 0.95 };

  static final double THROWABLE_PROPABILITY_FOR_WARNING = .1;
  static final double THROWABLE_PROPABILITY_FOR_ERRORS = .3;
  static final double NESTING_PROBABILITY = .5;

  static final int AVERAGE_LOGGER_NAME_PARTS = 6;
  static final int STD_DEV_FOR_LOGGER_NAME_PARTS = 3;

  static final int AVERAGE_MESSAGE_WORDS = 8;
  static final int STD_DEV_FOR_MESSAGE_WORDS = 4;

  static final int AVERAGE_MILLIS_INCREMENT = 10;
  static final int STD_DEV_FOR_MILLIS_INCREMENT = 5;

  static final int THREAD_POOL_SIZE = 10;
  static final int LOGGER_POOL_SIZE = 1000;
  static final int LOG_STATEMENT_POOL_SIZE = LOGGER_POOL_SIZE * 8;

  final Random random;
  List<String> worldList;
  String[] threadNamePool;

  LogStatement[] logStatementPool;

  // 2009-03-06 13:08 GMT
  long lastTimeStamp = 1236344888578L;

  public CorpusMaker(long seed, List<String> worldList) {
    random = new Random(seed);
    this.worldList = worldList;
    buildThreadNamePool();
    buildLogStatementPool();
  }

  private void buildThreadNamePool() {
    threadNamePool = new String[THREAD_POOL_SIZE];
    for (int i = 0; i < THREAD_POOL_SIZE; i++) {
      threadNamePool[i] = "CorpusMakerThread-" + i;
    }
  }

  private void buildLogStatementPool() {
    String[] loggerNamePool = new String[LOGGER_POOL_SIZE];
    for (int i = 0; i < LOGGER_POOL_SIZE; i++) {
      loggerNamePool[i] = makeRandomLoggerName();
    }
    logStatementPool = new LogStatement[LOG_STATEMENT_POOL_SIZE];
    for (int i = 0; i < LOG_STATEMENT_POOL_SIZE; i++) {
      logStatementPool[i] = makeRandomLogStatement(loggerNamePool);
    }
  }

  private int[] getRandomAnchorPositions(int wordCount, int numAnchors) {
    // note that the same position may appear multiple times in
    // positionsIndex, but without serious consequences
    int[] positionsIndex = new int[numAnchors];
    for (int i = 0; i < numAnchors; i++) {
      positionsIndex[i] = random.nextInt(wordCount);
    }
    return positionsIndex;
  }

  private String[] getRandomWords(int n) {
    String[] wordArray = new String[n];
    for (int i = 0; i < n; i++) {
      wordArray[i] = getRandomWord();
    }
    return wordArray;
  }

  public long getRandomLong() {
    return random.nextLong();
  }

  public String getRandomThreadNameFromPool() {
    int index = random.nextInt(THREAD_POOL_SIZE);
    return threadNamePool[index];
  }

  public LogStatement getRandomLogStatementFromPool() {
    int index = random.nextInt(logStatementPool.length);
    return logStatementPool[index];
  }

  private String getRandomLoggerNameFromPool(String[] loggerNamePool) {
    int index = random.nextInt(loggerNamePool.length);
    return loggerNamePool[index];
  }

  public long getRandomTimeStamp() {
    // subtract 1 so that 0 is allowed
    lastTimeStamp += RandomUtil.gaussianAsPositiveInt(random,
        AVERAGE_MILLIS_INCREMENT, STD_DEV_FOR_MILLIS_INCREMENT) - 1;
    return lastTimeStamp;
  }

  LoggerContextVO getRandomlyNamedLoggerContextVO() {
    LoggerContext lc = new LoggerContext();
    lc.setName(getRandomJavaIdentifier());
    return new LoggerContextVO(lc);
  }

  String getRandomWord() {
    int size = worldList.size();
    int randomIndex = random.nextInt(size);
    return worldList.get(randomIndex);
  }

  public Object[] getRandomArgumentArray(int numOfArguments) {
    if (numOfArguments == 0) {
      return null;
    }
    Object[] argumentArray = new Object[numOfArguments];
    for (int i = 0; i < numOfArguments; i++) {
      argumentArray[i] = new Long(random.nextLong());
    }
    return argumentArray;
  }

  private MessageItem makeRandomMessageEntry() {
    int numOfArguments = getNumberOfMessageArguments();

    int wordCount = RandomUtil.gaussianAsPositiveInt(random,
        AVERAGE_MESSAGE_WORDS, STD_DEV_FOR_MESSAGE_WORDS);
    String[] wordArray = getRandomWords(wordCount);

    int[] anchorPositions = getRandomAnchorPositions(wordCount, numOfArguments);

    for (int anchorIndex : anchorPositions) {
      wordArray[anchorIndex] = "{}";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < wordCount; i++) {
      sb.append(wordArray[i]).append(' ');
    }
    sb.append(getRandomWord());
    return new MessageItem(sb.toString(), numOfArguments);
  }

  private LogStatement makeRandomLogStatement(String[] loggerNamePool) {
    MessageItem mi = makeRandomMessageEntry();
    String loggerName = getRandomLoggerNameFromPool(loggerNamePool);
    Level randomLevel = getRandomLevel();
    Throwable t = getRandomThrowable(randomLevel);
    ThrowableProxyVO throwableProxy = null;
    if (t != null) {
      throwableProxy = ThrowableProxyVO.build(new ThrowableProxy(t));
      pupulateWithPackagingData(throwableProxy.getStackTraceElementProxyArray());
    }
    LogStatement logStatement = new LogStatement(loggerName, randomLevel, mi,
        throwableProxy);
    return logStatement;
  }

  private Throwable getRandomThrowable(Level level) {
    double rn = random.nextDouble();
    if ((level == Level.WARN && rn < THROWABLE_PROPABILITY_FOR_WARNING)
        || (level == Level.ERROR && rn < THROWABLE_PROPABILITY_FOR_ERRORS)) {
      return ExceptionBuilder.build(random, NESTING_PROBABILITY);
    } else {
      return null;
    }
  }

  private void pupulateWithPackagingData(StackTraceElementProxy[] stepArray) {
    int i = 0;
    for (StackTraceElementProxy step : stepArray) {
      String identifier = "na";
      String version = "na";
      if (i++ % 2 == 0) {
        identifier = getRandomJavaIdentifier();
        version = getRandomJavaIdentifier();
      }
      ClassPackagingData cpd = new ClassPackagingData(identifier, version);
      step.setClassPackagingData(cpd);
    }
  }
  
  private int getNumberOfMessageArguments() {
    double rn = random.nextDouble();
    if (rn < ARGUMENT_DISTRIBUTION[0]) {
      return 0;
    }
    if (rn < ARGUMENT_DISTRIBUTION[1]) {
      return 1;
    }
    if (rn < ARGUMENT_DISTRIBUTION[2]) {
      return 2;
    }
    return 3;
  }

  String getRandomJavaIdentifier() {
    String w = getRandomWord();
    w = w.replaceAll("\\p{Punct}", "");
    return w;
  }

  private String makeRandomLoggerName() {
    int parts = RandomUtil.gaussianAsPositiveInt(random,
        AVERAGE_LOGGER_NAME_PARTS, STD_DEV_FOR_LOGGER_NAME_PARTS);
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < parts; i++) {
      sb.append(getRandomJavaIdentifier()).append('.');
    }
    sb.append(getRandomJavaIdentifier());
    return sb.toString();
  }

  private Level getRandomLevel() {
    double rn = random.nextDouble();
    if (rn < LEVEL_DISTRIBUTION[0]) {
      return Level.TRACE;
    }
    if (rn < LEVEL_DISTRIBUTION[1]) {
      return Level.DEBUG;
    }

    if (rn < LEVEL_DISTRIBUTION[2]) {
      return Level.INFO;
    }

    if (rn < LEVEL_DISTRIBUTION[3]) {
      return Level.WARN;
    }

    return Level.ERROR;
  }
}
