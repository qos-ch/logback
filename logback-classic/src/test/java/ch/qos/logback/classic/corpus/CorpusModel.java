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

/**
 * Models the corpus.
 * 
 * <p>This contains the probability distributions of levels, logger names,
 * messages, message arguments.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class CorpusModel {

    // N(u,s) denotes a random variable normally distributed with mean u and
    // variance sqrt(s), where sqrt() is the square root function. For an
    // explanation of normal distribution please see
    // http://en.wikipedia.org/wiki/Normal_distribution

    // It is assumed that the number of parts in a logger name is a random
    // variable normally distributed with mean AVERAGE_LOGGER_NAME_PARTS and
    // standard deviation STD_DEV_FOR_LOGGER_NAME_PARTS
    static final int AVERAGE_LOGGER_NAME_PARTS = 6;
    static final int STD_DEV_FOR_LOGGER_NAME_PARTS = 3;

    // It is assumed that there are LOGGER_POOL_SIZE logger names
    // in our model corpus.
    static final int LOGGER_POOL_SIZE = 1000;

    // It is assumed that there are LOG_STATEMENT_POOL_SIZE log statements
    // in our model corpus.
    static final int LOG_STATEMENT_POOL_SIZE = LOGGER_POOL_SIZE * 8;

    // level distribution is determined by the following table
    // It corresponds to TRACE 3%, DEBUG 30%, INFO 30%, WARN 5%,
    // ERROR 5%. See also getRandomLevel() method.
    static final double[] LEVEL_DISTRIBUTION = new double[] { .3, .3, .9, .95 };

    // It is assumed that the number of words in the message (contained in a log
    // statement) is a random variable normally distributed with mean
    // AVERAGE_MESSAGE_WORDS and standard deviation STD_DEV_FOR_MESSAGE_WORDS
    static final int AVERAGE_MESSAGE_WORDS = 8;
    static final int STD_DEV_FOR_MESSAGE_WORDS = 4;

    // messages will have no arguments 80% of the time, one argument in 8%, two
    // arguments in 7% and three arguments in 5% of cases
    static final double[] ARGUMENT_DISTRIBUTION = new double[] { .80, .88, 0.95 };

    static final double THROWABLE_PROPABILITY_FOR_WARNING = .1;
    static final double THROWABLE_PROPABILITY_FOR_ERRORS = .3;
    // .5 of throwables are nested once
    static final double NESTING_PROBABILITY = .5;

    // For each logging event the timer is incremented by a certain value. it is
    // assumed that this value is a random variable normally distributed with mean
    // AVERAGE_MILLIS_INCREMENT and standard deviation
    // STD_DEV_FOR_MILLIS_INCREMENT
    static final int AVERAGE_MILLIS_INCREMENT = 10;
    static final int STD_DEV_FOR_MILLIS_INCREMENT = 5;

    // assume that there are THREAD_POOL_SIZE threads in the corpus
    static final int THREAD_POOL_SIZE = 10;

    final Random random;
    final List<String> worldList;
    String[] threadNamePool;
    LogStatement[] logStatementPool;
    String[] loggerNamePool;

    // 2009-03-06 13:08 GMT
    long lastTimeStamp = 1236344888578L;

    public CorpusModel(long seed, List<String> worldList) {
        random = new Random(seed);
        this.worldList = worldList;
        buildThreadNamePool();
        buildLoggerNamePool();
        buildLogStatementPool();
    }

    private void buildThreadNamePool() {
        threadNamePool = new String[THREAD_POOL_SIZE];
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            threadNamePool[i] = "CorpusMakerThread-" + i;
        }
    }

    private void buildLoggerNamePool() {
        loggerNamePool = new String[LOGGER_POOL_SIZE];
        for (int i = 0; i < LOGGER_POOL_SIZE; i++) {
            loggerNamePool[i] = makeRandomLoggerName();
        }
    }

    private void buildLogStatementPool() {
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
        lastTimeStamp += RandomUtil.gaussianAsPositiveInt(random, AVERAGE_MILLIS_INCREMENT, STD_DEV_FOR_MILLIS_INCREMENT) - 1;
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

    String extractLastPart(String loggerName) {
        int i = loggerName.lastIndexOf('.');
        if (i == -1) {
            return loggerName;
        } else {
            return loggerName.substring(i + 1);
        }
    }

    public StackTraceElement[] getRandomCallerData(int depth, String loggerName) {
        StackTraceElement[] cda = new StackTraceElement[depth];
        StackTraceElement cd = new StackTraceElement(loggerName, getRandomJavaIdentifier(), extractLastPart(loggerName), 0);
        cda[0] = cd;
        for (int i = 1; i < depth; i++) {
            String ln = getRandomLoggerNameFromPool(loggerNamePool);
            cda[i] = new StackTraceElement(ln, getRandomJavaIdentifier(), extractLastPart(ln), i * 10);
        }
        return cda;
    }

    public Object[] getRandomArgumentArray(int numOfArguments) {
        if (numOfArguments == 0) {
            return null;
        }
        Object[] argumentArray = new Object[numOfArguments];
        for (int i = 0; i < numOfArguments; i++) {
            argumentArray[i] = Long.valueOf(random.nextLong());
        }
        return argumentArray;
    }

    private MessageArgumentTuple makeRandomMessageArgumentTuple() {
        int numOfArguments = getNumberOfMessageArguments();

        int wordCount = RandomUtil.gaussianAsPositiveInt(random, AVERAGE_MESSAGE_WORDS, STD_DEV_FOR_MESSAGE_WORDS);
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
        return new MessageArgumentTuple(sb.toString(), numOfArguments);
    }

    private LogStatement makeRandomLogStatement(String[] loggerNamePool) {
        MessageArgumentTuple mat = makeRandomMessageArgumentTuple();
        String loggerName = getRandomLoggerNameFromPool(loggerNamePool);
        Level randomLevel = getRandomLevel();
        Throwable t = getRandomThrowable(randomLevel);
        ThrowableProxyVO throwableProxy = null;
        if (t != null) {
            throwableProxy = ThrowableProxyVO.build(new ThrowableProxy(t));
            pupulateWithPackagingData(throwableProxy.getStackTraceElementProxyArray());
        }
        return new LogStatement(loggerName, randomLevel, mat, throwableProxy);
    }

    private Throwable getRandomThrowable(Level level) {
        double rn = random.nextDouble();
        if ((level == Level.WARN && rn < THROWABLE_PROPABILITY_FOR_WARNING) || (level == Level.ERROR && rn < THROWABLE_PROPABILITY_FOR_ERRORS)) {
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
        int parts = RandomUtil.gaussianAsPositiveInt(random, AVERAGE_LOGGER_NAME_PARTS, STD_DEV_FOR_LOGGER_NAME_PARTS);
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
