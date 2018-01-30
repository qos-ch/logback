package ch.qos.logback.classic.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;

public class LogbackServletContainerInitializerTest {

    LogbackServletContainerInitializer lsci = new LogbackServletContainerInitializer();
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOnStartup() throws ServletException {
        ServletContext mockedServletContext = mock(ServletContext.class);
        lsci.onStartup(null, mockedServletContext);
        verify(mockedServletContext).addListener(any(LogbackServletContextListener.class));
    }

    @Test
    public void noListenerShouldBeAddedWhenDisabled() throws ServletException {
        ServletContext mockedServletContext = mock(ServletContext.class);
        when(mockedServletContext.getInitParameter(CoreConstants.DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY)).thenReturn("true");
        lsci.onStartup(null, mockedServletContext);
        verify(mockedServletContext, times(0)).addListener(any(LogbackServletContextListener.class));
    }

}
