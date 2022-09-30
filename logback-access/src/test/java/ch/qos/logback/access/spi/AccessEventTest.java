package ch.qos.logback.access.spi;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessEventTest {

    int diff = RandomUtil.getPositiveInt();

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    // See LOGBACK-1189
    @Test
    public void callingPrepareForDeferredProcessingShouldBeIdempotent() {
        String key = "key-" + diff;
        String val = "val-" + diff;

        IAccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();
        DummyRequest request = (DummyRequest) ae.getRequest();
        Map<String, String> headersMap = request.getHeaders();
        Map<String, String[]> parametersMap = request.getParameterMap();

        headersMap.put(key, val);
        request.setAttribute(key, val);
        parametersMap.put(key, new String[] { val });
        ae.prepareForDeferredProcessing();
        assertEquals(val, ae.getAttribute(key));
        assertEquals(val, ae.getRequestHeader(key));
        assertEquals(val, ae.getRequestParameter(key)[0]);

        request.setAttribute(key, "change");
        headersMap.put(key, "change");
        parametersMap.put(key, new String[] { "change" });
        ae.prepareForDeferredProcessing();
        assertEquals(val, ae.getAttribute(key));
        assertEquals(val, ae.getRequestHeader(key));
        assertEquals(val, ae.getRequestParameter(key)[0]);

    }

}
