package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class EpochConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String millisOrSeconds = getFirstOption();
        if ("seconds".equals(millisOrSeconds) ) {
            return "" + (event.getTimeStamp() / 1000L);
        }

        return "" + event.getTimeStamp();
    }
}
