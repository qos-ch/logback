package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.Before;
import org.junit.Test;

/**
 * @author szalik
 */
public class HasThrowableTurboFilterTest {
    private HasThrowableTurboFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new HasThrowableTurboFilter();
    }

    @Test
    public void testAccept() {
        filter.start();
        FilterReply fr = filter.decide(null, null, Level.DEBUG, "a", new Object[0], new RuntimeException());
        org.junit.Assert.assertEquals(FilterReply.ACCEPT, fr);
    }

    @Test
    public void testReject() {
        filter.start();
        FilterReply fr = filter.decide(null, null, Level.DEBUG, "a", new Object[0], null);
        org.junit.Assert.assertEquals(FilterReply.DENY, fr);
    }

    @Test
    public void testNotStarted1() {
        FilterReply fr = filter.decide(null, null, Level.DEBUG, "a", new Object[0], null);
        org.junit.Assert.assertEquals(FilterReply.NEUTRAL, fr);
    }

    @Test
    public void testNotStarted2() {
        FilterReply fr = filter.decide(null, null, Level.DEBUG, "a", new Object[0], new Exception());
        org.junit.Assert.assertEquals(FilterReply.NEUTRAL, fr);
    }

}
