package ch.qos.logback.classic.corpus;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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
 * <p>
 * <code>Corpus.dump(eventArray, "/corpus.log");
 * 
 * <p>For the model behind the corpus, refer to {@link CorpusModel}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class Corpus {

  static public final int STANDARD_CORPUS_SIZE = 50 * 1000;
  private static final int STANDARD_SEED = 34780;

  /**
   * Make a standard corpus. The standard corpus has
   * {@link #STANDARD_CORPUS_SIZE} elements.
   * 
   * @return event array representing the standard corpus
   * @throws IOException
   */
  static public ILoggingEvent[] makeStandardCorpus() throws IOException {
    ClassLoader classLoader = Corpus.class.getClassLoader();
    URL originOfSpeciesURL = classLoader
        .getResource("corpus/origin_of_species.txt");
    List<String> worldList = TextFileUtil.toWords(originOfSpeciesURL);
    CorpusModel corpusMaker = new CorpusModel(STANDARD_SEED, worldList);
    return make(corpusMaker, STANDARD_CORPUS_SIZE);
  }

  static public ILoggingEvent[] make(CorpusModel corpusMaker, int n) {
    LoggerContextVO lcVO = corpusMaker.getRandomlyNamedLoggerContextVO();
    PubLoggingEventVO[] plevoArray = new PubLoggingEventVO[n];
    for (int i = 0; i < n; i++) {
      PubLoggingEventVO e = new PubLoggingEventVO();
      plevoArray[i] = e;
      e.loggerContextVO = lcVO;
      e.timeStamp = corpusMaker.getRandomTimeStamp();

      LogStatement logStatement = corpusMaker.getRandomLogStatementFromPool();
      e.loggerName = logStatement.loggerName;
      e.level = logStatement.level;
      e.message = logStatement.mat.message;
      e.argumentArray = corpusMaker
          .getRandomArgumentArray(logStatement.mat.numberOfArguments);
      e.throwableProxy = logStatement.throwableProxy;
      e.threadName = corpusMaker.getRandomThreadNameFromPool();
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
  public static void dump(ILoggingEvent[] eventArray, String targetFile)
      throws IOException {
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
