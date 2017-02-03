package ch.qos.logback.access.spi;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.slf4j.MDC;

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

    @Test
    public void testMDCPropertyMap() throws Exception {

        assertNotNull(MDC.getMDCAdapter());

        // empty MDC
        {
            MDC.clear();

            IAccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();
            Map<String, String> map = ae.getMDCPropertyMap();
            assertTrue(map.isEmpty());

            MDC.put("key", "value");

            // test idempotence
            Map<String, String> map1 = ae.getMDCPropertyMap();
            assertSame(map, map1);
            assertTrue(map1.isEmpty());
        }

        {
            MDC.clear();
            MDC.put("key", "value");

            IAccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();
            Map<String, String> map = ae.getMDCPropertyMap();
            assertEquals(1, map.size());
            assertSame(MDC.get("key"), map.get("key"));

            MDC.put("k", "v");

            // test idempotence
            Map<String, String> map1 = ae.getMDCPropertyMap();
            assertSame(map, map1);
        }

        {
            Map<String, String> map = new HashMap<String, String>();

            AccessEvent ae = (AccessEvent) DummyAccessEventBuilder.buildNewAccessEvent();
            ae.setMDCPropertyMap(map);

            assertSame(map, ae.getMDCPropertyMap());

            // no further update
            try{
                ae.setMDCPropertyMap(map);
                fail();
            }
            catch( IllegalStateException e){
                // OK
            }
        }
    }
}
