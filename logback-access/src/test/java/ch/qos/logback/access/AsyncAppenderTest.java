package ch.qos.logback.access;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AsyncAppenderTest {

    private static Random random = new Random();
    private ListAppender<IAccessEvent> listAppender = new ListAppender<IAccessEvent>();
    private OnConsoleStatusListener onConsoleStatusListener = new OnConsoleStatusListener();
    private AsyncAppender asyncAppender = new AsyncAppender();

    private static IAccessEvent createAccessEvent(final String uri) {
        final DummyRequest request = new DummyRequest();
        DummyResponse response = new DummyResponse();
        DummyServerAdapter adapter = new DummyServerAdapter(request, response);

        return new AccessEvent(request, response, adapter) {
            @Override
            public void prepareForDeferredProcessing() {
                request.setRequestUri(uri);
                super.prepareForDeferredProcessing();
            }
        };
    }

    @Before
    public void setUp() {
        onConsoleStatusListener.start();

        listAppender.setName("list");
        listAppender.start();
    }

    @Test
    public void eventWasPreparedForDeferredProcessing() {
        asyncAppender.addAppender(listAppender);
        asyncAppender.start();

        String uri = "uri:" + random.nextLong();
        IAccessEvent dummyAccessEvent = createAccessEvent(uri);

        asyncAppender.doAppend(dummyAccessEvent);

        asyncAppender.stop();
        assertFalse(asyncAppender.isStarted());

        assertEquals(1, listAppender.list.size());
        IAccessEvent e = listAppender.list.get(0);

        assertEquals(uri, e.getRequestURI());
    }

    @Test
    public void accessEventIsNeverDiscardable() throws Exception {
        final IAccessEvent dummyAccessEvent = createAccessEvent("");
        assertFalse(asyncAppender.isDiscardable(dummyAccessEvent));
    }
}
