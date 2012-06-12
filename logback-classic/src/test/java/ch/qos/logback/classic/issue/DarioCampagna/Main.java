package ch.qos.logback.classic.issue.DarioCampagna;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import java.util.Locale;

public class Main {
  public static void main(String[] args) throws JoranException {

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.reset();
    JoranConfigurator joranConfigurator = new JoranConfigurator();
    joranConfigurator.setContext(context);
    joranConfigurator.doConfigure("src/test/java/ch/qos/logback/classic/issue/DarioCampagna/logback-marker.xml");
    IMessageConveyor mc = new MessageConveyor(Locale.getDefault());
    LocLoggerFactory llFactory_default = new LocLoggerFactory(mc);
    LocLogger locLogger = llFactory_default.getLocLogger("defaultLocLogger");
    Marker alwaysMarker = MarkerFactory.getMarker("ALWAYS");
    locLogger.info(alwaysMarker, "This will always appear.");
    locLogger.info("Hello!");
  }
}
