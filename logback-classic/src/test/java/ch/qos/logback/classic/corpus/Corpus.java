package ch.qos.logback.classic.corpus;

import java.io.IOException;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.PubLoggingEventVO;

public class Corpus {
  
  static final int STANDARD_CORPUS_SIZE = 50 * 1000;
  static final int STANDARD_SEED = 1123;

  static public ILoggingEvent[] make(CorpusMaker corpusMaker, int n) {
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
      e.message = logStatement.messagerItem.message;
      e.argumentArray = corpusMaker.getRandomArgumentArray(logStatement.messagerItem.numberOfArguments);
      e.throwableProxy = logStatement.throwableProxy;
      e.threadName = corpusMaker.getRandomThreadNameFromPool();
    }
    return plevoArray;
  }

  static  public ILoggingEvent[] makeStandardCorpus() throws IOException {
    List<String> worldList = TextFileUtil
        .toWords("src/test/input/corpus/origin_of_species.txt");
    CorpusMaker corpusMaker = new CorpusMaker(STANDARD_SEED, worldList);
    return make(corpusMaker, STANDARD_CORPUS_SIZE);
  }

}
