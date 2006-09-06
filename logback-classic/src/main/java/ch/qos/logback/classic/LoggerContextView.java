package ch.qos.logback.classic;

import java.util.Map;

public interface LoggerContextView {

	public String getName();
	public Map<String, String> getPropertyMap();
}
