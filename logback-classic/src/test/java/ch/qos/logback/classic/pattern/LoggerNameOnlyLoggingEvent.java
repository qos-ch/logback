package ch.qos.logback.classic.pattern;

import java.util.List;
import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;

public class LoggerNameOnlyLoggingEvent implements ILoggingEvent {

	String loggerName = "";
	
	@Override
	public String getThreadName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Level getLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	@Override
	public LoggerContextVO getLoggerContextVO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IThrowableProxy getThrowableProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StackTraceElement[] getCallerData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCallerData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Marker> getMarkerList() {
		return null;
	}

	@Override
	public Map<String, String> getMDCPropertyMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getMdc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeStamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSequenceNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void prepareForDeferredProcessing() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Object[] getArgumentArray() {
		return null;
	}

	@Override
	public String getFormattedMessage() {
		return null;
	}

	@Override
	public List<KeyValuePair> getKeyValuePairs() {
		return null;
	}


}
