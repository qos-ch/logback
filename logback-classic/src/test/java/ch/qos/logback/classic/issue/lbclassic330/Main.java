package ch.qos.logback.classic.issue.lbclassic330;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

  static Logger logger = LoggerFactory.getLogger(Main.class);
   static String DIR_PREFIX = "src/test/java/ch/qos/logback/classic/issue/lbclassic330/";

   public static void main(String[] args) throws JoranException, InterruptedException {
     init(DIR_PREFIX + "logback.xml");
     logger.debug("hello");
   }


   static void init(String file) throws JoranException {
     LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
     JoranConfigurator jc = new JoranConfigurator();
     jc.setContext(loggerContext);
     loggerContext.reset();
     jc.doConfigure(file);
   }
}
