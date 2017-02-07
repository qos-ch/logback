package ch.qos.logback.classic.net.server;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.helpers.BasicMarker;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.classic.spi.ThrowableProxyVO;

public class LogbackClassicSerializationHelper {

    
    
    static public List<String> getWhilelist() {
        List<String> whitelist = new ArrayList<String>();
        whitelist.add(LoggingEventVO.class.getName());
        whitelist.add(LoggerContextVO.class.getName());
        whitelist.add(ThrowableProxyVO.class.getName());
        whitelist.add(StackTraceElement.class.getName());
        whitelist.add(BasicMarker.class.getName());
        whitelist.add(BasicMarker.class.getName());
        whitelist.add(Logger.class.getName());
        return whitelist;
    }
}
