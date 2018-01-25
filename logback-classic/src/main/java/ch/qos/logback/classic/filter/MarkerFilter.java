package ch.qos.logback.classic.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

public class MarkerFilter extends AbstractMatcherFilter<ILoggingEvent> {

    private String marker;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        Marker currentMarker = event.getMarker();

        if (currentMarker == null || !marker.equals(currentMarker.getName())) {
            return onMismatch;
        } else {
            return onMatch;
        }
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    @Override
    public void start() {
        if (this.marker != null) {
            super.start();
        }
    }

}
