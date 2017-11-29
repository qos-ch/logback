package ch.qos.logback.classic.provider;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class LogbackServiceProvider implements SLF4JServiceProvider {
    
    /**
     * Declare the version of the SLF4J API this implementation is compiled against. 
     * The value of this field is modified with each major release. 
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.8.99"; // !final

    
    @Override
    public ILoggerFactory getLoggerFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequesteApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
        
    }

}
