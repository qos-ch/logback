package ch.qos.logback.classic;

import java.io.Serializable;
import java.util.Map;

/**
 * LoggerContextRemoteView offers a restricted view of LoggerContext intended to be 
 * exposed by LoggingEvent. This restricted view is optimiyed for serialisation.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggerContextRemoteView implements Serializable {

	
	private static final long serialVersionUID = 5488023392483144387L;

	final String name;
	final Map<String, String> propertyMap;

	
	public LoggerContextRemoteView(LoggerContext lc) {
		this(lc.getName(), lc.getPropertyMap());
	}
	
	public LoggerContextRemoteView(String name, Map<String, String> propertyMap) {
		this.name = name;
		this.propertyMap = propertyMap;
	}
	
	public String getName() {
		return name;
	}
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

}
