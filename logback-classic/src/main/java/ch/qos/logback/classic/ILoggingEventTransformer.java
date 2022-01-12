package ch.qos.logback.classic;

import ch.qos.logback.classic.spi.ILoggingEvent;

@FunctionalInterface
public interface ILoggingEventTransformer {

    ILoggingEvent transform(ILoggingEvent event);

}