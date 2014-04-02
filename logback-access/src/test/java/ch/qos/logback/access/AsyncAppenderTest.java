package ch.qos.logback.access;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class AsyncAppenderTest {

    AsyncAppender asyncAppender = new AsyncAppender();

    private static IAccessEvent createAccessEvent() {
        DummyRequest request = new DummyRequest();
        request.setRequestUri("");
        DummyResponse response = new DummyResponse();
        DummyServerAdapter adapter = new DummyServerAdapter(request, response);

        return new AccessEvent(request, response, adapter);
    }

    @Test
    public void accessEventIsNeverDiscardable() throws Exception {
        final IAccessEvent dummyAccessEvent = createAccessEvent();
        assertFalse(asyncAppender.isDiscardable(dummyAccessEvent));
    }
}
