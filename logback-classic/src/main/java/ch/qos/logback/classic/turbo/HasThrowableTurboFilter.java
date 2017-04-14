package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

/**
 * Logback turbo filter that accepts all events with Throwable attached
 * @author szalik
 */
public class HasThrowableTurboFilter extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (! isStarted()) {
            return FilterReply.NEUTRAL;
        }
        return t == null ? FilterReply.DENY : FilterReply.ACCEPT;
    }
}
