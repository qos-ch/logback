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

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.PubLoggingEventVO;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

/**
 * 
 * <p>Usage:
 * 
 * <p><code>ILoggingEvent[] eventArray = Corpus.makeStandardCorpus();</code>
 * 
 * <p>if you wish to dump the events into a file, say "/corpus.log" :
 * 
 * <p> <code>Corpus.dump(eventArray, "/corpus.log");
 * 
 * <p>For the model behind the corpus, refer to {@link CorpusModel}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class Corpus {

    static public final int STANDARD_CORPUS_SIZE = 50 * 1000;
    private static final int STANDARD_SEED = 34780;

    static public List<String> getStandatdCorpusWordList() throws IOException {
        ClassLoader classLoader = Corpus.class.getClassLoader();
        URL originOfSpeciesURL = classLoader.getResource("corpus/origin_of_species.txt");
        return TextFileUtil.toWords(originOfSpeciesURL);
    }

    /**
     * Make a standard corpus. The standard corpus has
     * {@link #STANDARD_CORPUS_SIZE} elements.
     * 
     * @return event array representing the standard corpus
     * @throws IOException
     */
    static public ILoggingEvent[] makeStandardCorpus() throws IOException {
        List<String> worldList = getStandatdCorpusWordList();
        CorpusModel corpusMaker = new CorpusModel(STANDARD_SEED, worldList);
        return make(corpusMaker, STANDARD_CORPUS_SIZE, true);
    }

    static public ILoggingEvent[] make(CorpusModel corpusModel, int n, boolean withCallerData) {
        LoggerContextVO lcVO = corpusModel.getRandomlyNamedLoggerContextVO();
        PubLoggingEventVO[] plevoArray = new PubLoggingEventVO[n];
        for (int i = 0; i < n; i++) {
            PubLoggingEventVO e = new PubLoggingEventVO();
            plevoArray[i] = e;
            e.loggerContextVO = lcVO;
            e.timeStamp = corpusModel.getRandomTimeStamp();

            LogStatement logStatement = corpusModel.getRandomLogStatementFromPool();
            e.loggerName = logStatement.loggerName;
            e.level = logStatement.level;
            e.message = logStatement.mat.message;
            e.argumentArray = corpusModel.getRandomArgumentArray(logStatement.mat.numberOfArguments);

            if (withCallerData) {
                e.callerDataArray = corpusModel.getRandomCallerData(ClassicConstants.DEFAULT_MAX_CALLEDER_DATA_DEPTH, e.loggerName);
            }
            e.throwableProxy = logStatement.throwableProxy;
            e.threadName = corpusModel.getRandomThreadNameFromPool();
        }
        return plevoArray;
    }

    /**
     * Dump the events passed as argument into the file named targetFile.
     * 
     * @param eventArray
     * @param targetFile
     * @throws IOException
     */
    public static void dump(ILoggingEvent[] eventArray, String targetFile) throws IOException {
        FileWriter fw = new FileWriter(targetFile);
        for (ILoggingEvent e : eventArray) {
            fw.write(e.toString());
            fw.append(CoreConstants.LINE_SEPARATOR);
            if (e.getThrowableProxy() != null) {
                IThrowableProxy tp = e.getThrowableProxy();
                fw.write(ThrowableProxyUtil.asString(tp));
            }
        }
        fw.flush();
        fw.close();
    }

}
