package ch.qos.logback.classic.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import ch.qos.logback.core.CoreConstants;
import org.junit.jupiter.api.Test;

public class LogbackServletContainerInitializerTest {

    LogbackServletContainerInitializer lsci = new LogbackServletContainerInitializer();

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
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
        when(mockedServletContext.getInitParameter(CoreConstants.DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY))
                .thenReturn("true");
        lsci.onStartup(null, mockedServletContext);
        verify(mockedServletContext, times(0)).addListener(any(LogbackServletContextListener.class));
    }

}
