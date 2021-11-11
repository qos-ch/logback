package ch.qos.logback.classic.issue.logback_1162;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) throws InterruptedException, JoranException {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        lc.putProperty("output_dir", ClassicTestConstants.OUTPUT_DIR_PREFIX + "logback_issue_1162/");

        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        configurator.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1162.xml");

        logger.info("Hello, world!");

        TimeUnit.SECONDS.sleep(0);
    }
}
