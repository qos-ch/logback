package ch.qos.logback.core.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;

public class StatusListenerConfigHelperTest {

    Context context = new ContextBase();
    StatusManager sm = context.getStatusManager();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addOnConsoleListenerInstanceShouldNotStartSecondListener() {
        OnConsoleStatusListener ocl0 = new OnConsoleStatusListener();
        OnConsoleStatusListener ocl1 = new OnConsoleStatusListener();

        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, ocl0);
        {
            List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
            assertTrue(ocl0.isStarted());
        }

        // second listener should not have been started
        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, ocl1);
        {
            List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
            assertEquals(1, listeners.size());
            assertFalse(ocl1.isStarted());
        }
    }

}
