package ch.qos.logback.classic.rolling;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * @author Stanislav Doktorovich
 * @since 2017-07-07
 */
public class ManualRolloverTest
{
    @BeforeClass
    public static void configureLogback()
    {
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try
        {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            String config = "<configuration debug=\"false\" scan=\"false\">\n" +
                            "    <appender name=\"FILE\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
                            "        <file>1.log</file>\n" +
                            "        <rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">\n" +
                            "            <!-- daily rollover -->\n" +
                            "            <fileNamePattern>1.log.%d{yyyyMMdd}</fileNamePattern>\n" +
                            "            <maxHistory>3</maxHistory>\n" +
                            "            <totalSizeCap>1GB</totalSizeCap>\n" +
                            "        </rollingPolicy>\n" +
                            "    </appender>\n" +
                            "    <root level=\"INFO\">\n" +
                            "        <appender-ref ref=\"FILE\" />\n" +
                            "    </root>\n" +
                            "</configuration>";
            configurator.doConfigure(new ByteArrayInputStream(config.getBytes()));
        }
        catch (JoranException je)
        {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    @Test
    public void checkManualRolloverDoesNotThrowNPE() throws Exception
    {

        // assume SLF4J is bound to logback in the current environment
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger logger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rollover(logger.iteratorForAppenders());
    }

    private static void rollover(Iterator<Appender<ILoggingEvent>> it)
    {
        while (it.hasNext())
        {
            Appender<ILoggingEvent> appender = it.next();
            if (appender instanceof RollingFileAppender)
            {
                RollingFileAppender rollingFileAppender = (RollingFileAppender) appender;
                rollingFileAppender.rollover();
            }
            else if (appender instanceof AsyncAppender)
            {
                AsyncAppender asyncAppender = (AsyncAppender) appender;
                rollover(asyncAppender.iteratorForAppenders());
            }
        }
    }
}
