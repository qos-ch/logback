package ch.qos.logback.classic.issue.logback_1361;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static String ONE_KB_STRING;
    
    public static void main(String[] args) throws Exception {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        lc.putProperty("output_dir", ClassicTestConstants.OUTPUT_DIR_PREFIX+"logback_issue_1361/");
        
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        configurator.doConfigure(ClassicTestConstants.INPUT_PREFIX+ "issue/logback_1361.xml");

        log1MegaByteInOneSecond();
    }
    
    static {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 100; j++) {
            String message = "1234567890";
            sb.append(message);
        }
        ONE_KB_STRING = sb.toString();
    }


    private static void log1MegaByteInOneSecond() throws Exception {
        for (int i = 0; i < 1000; i++) {
            logger.warn(i + " - " + ONE_KB_STRING);
            Thread.sleep(1);
        }
    }


}
