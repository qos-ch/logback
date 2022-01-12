package ch.qos.logback.classic;

import ch.qos.logback.classic.spi.ILoggingEvent;

public interface ILoggingEventTransformer {

    ILoggingEvent transform(ILoggingEvent event);

}