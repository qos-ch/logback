package ch.qos.logback.access.spi;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.core.testUtil.RandomUtil;

public class AccessEventTest {

    int diff = RandomUtil.getPositiveInt();
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    // See LOGBACK-1189
    @Test
    public void callingPrepareForDeferredProcessingShouldBeIdempotent() {
        String key = "key-"+diff;
        String val = "val-"+diff;
        
        IAccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();
        DummyRequest request = (DummyRequest) ae.getRequest();
        Map<String, String> headersMap = request.getHeaders();
        Map<String, String[]> parametersMap = request.getParameterMap();
        
        headersMap.put(key, val);
        request.setAttribute(key, val);
        parametersMap.put(key, new String[] {val});
        ae.prepareForDeferredProcessing();
        assertEquals(val, ae.getAttribute(key));
        assertEquals(val, ae.getRequestHeader(key));
        assertEquals(val, ae.getRequestParameter(key)[0]);
        
        
        request.setAttribute(key, "change");
        headersMap.put(key, "change");
        parametersMap.put(key, new String[] {"change"});
        ae.prepareForDeferredProcessing();
        assertEquals(val, ae.getAttribute(key));
        assertEquals(val, ae.getRequestHeader(key));
        assertEquals(val, ae.getRequestParameter(key)[0]);
        
    }

}
