package ch.qos.logback.classic.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.Test;
import org.slf4j.Marker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarkerFilterTest {

    @Test
    public void filterIgnoresLoggingEventIfNotStarted() {
        // given
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        MarkerFilter filter = new MarkerFilter();

        // when
        FilterReply filterReply = filter.decide(loggingEvent);

        // then
        assertThat(filterReply).isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    public void filterIgnoresLoggingEventIfNoMarkerWasSet() {
        // given
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        MarkerFilter filter = new MarkerFilter();

        filter.start();

        // when
        FilterReply filterReply = filter.decide(loggingEvent);

        // then
        assertThat(filterReply).isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    public void filterDeniesLoggingEventIfItHasNoMarker() {
        // given
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        MarkerFilter filter = new MarkerFilter();

        filter.setOnMismatch(FilterReply.DENY);
        filter.setMarker("MARKER");
        filter.start();

        // when
        FilterReply filterReply = filter.decide(loggingEvent);

        // then
        assertThat(filterReply).isEqualTo(FilterReply.DENY);
    }

    @Test
    public void filterDeniesLoggingEventIfMarkerHasNoName() {
        // given
        Marker marker = mock(Marker.class);
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        MarkerFilter filter = new MarkerFilter();

        when(loggingEvent.getMarker()).thenReturn(marker);
        filter.setOnMismatch(FilterReply.DENY);
        filter.setMarker("MARKER");
        filter.start();

        // when
        FilterReply filterReply = filter.decide(loggingEvent);

        // then
        assertThat(filterReply).isEqualTo(FilterReply.DENY);
    }

    @Test
    public void filterDeniesLoggingEventIfMarkerHasADifferentName() {
        // given
        Marker marker = mock(Marker.class);
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        MarkerFilter filter = new MarkerFilter();

        when(marker.getName()).thenReturn("INVALID_MARKER");
        when(loggingEvent.getMarker()).thenReturn(marker);
        filter.setOnMismatch(FilterReply.DENY);
        filter.setMarker("MARKER");
        filter.start();

        // when
        FilterReply filterReply = filter.decide(loggingEvent);

        // then
        assertThat(filterReply).isEqualTo(FilterReply.DENY);
    }

    @Test
    public void filterAcceptssLoggingEventIfMarkerMatches() {
        // given
        Marker marker = mock(Marker.class);
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        MarkerFilter filter = new MarkerFilter();

        when(marker.getName()).thenReturn("MARKER");
        when(loggingEvent.getMarker()).thenReturn(marker);
        filter.setOnMatch(FilterReply.ACCEPT);
        filter.setMarker("MARKER");
        filter.start();

        // when
        FilterReply filterReply = filter.decide(loggingEvent);

        // then
        assertThat(filterReply).isEqualTo(FilterReply.ACCEPT);
    }

}
