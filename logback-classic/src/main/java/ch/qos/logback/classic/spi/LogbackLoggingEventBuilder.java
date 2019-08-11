package ch.qos.logback.classic.spi;

import java.util.function.Supplier;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import ch.qos.logback.classic.Logger;

public class LogbackLoggingEventBuilder implements LoggingEventBuilder {

	public LogbackLoggingEventBuilder(Logger logger, Level level) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public LoggingEventBuilder setCause(Throwable cause) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggingEventBuilder addMarker(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggingEventBuilder addArgument(Object p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggingEventBuilder addArgument(Supplier<Object> objectSupplier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggingEventBuilder addKeyValue(String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggingEventBuilder addKeyValue(String key, Supplier<Object> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(String message, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(String message, Object arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(String message, Object... args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(Supplier<String> messageSupplier) {
		// TODO Auto-generated method stub

	}

}
