package ch.qos.logback.classic.spi;

import java.io.Serializable;
import java.util.Map;

import ch.qos.logback.classic.LoggerContext;

/**
 * LoggerContextRemoteView offers a restricted view of LoggerContext intended to be 
 * exposed by LoggingEvent. This restricted view is optimised for serialisation.
 * 
 * Some of the LoggerContext or Logger attributes should not survive 
 * serialization, e.g appenders, level values etc, as these attributes may 
 * have other values on the remote platform. LoggerContextRemoteView class exposes 
 * the minimal (relevant) attributes to remote host, instead of having to deal with 
 * an incomplete LoggerContext with many null references.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggerContextRemoteView implements Serializable {

	
	private static final long serialVersionUID = 5488023392483144387L;

	final String name;
	final Map<String, String> propertyMap;

	
	public LoggerContextRemoteView(LoggerContext lc) {
		//this(lc.getName(), lc.getPropertyMap());
		this.name = lc.getName();
		this.propertyMap = lc.getPropertyMap();
	}
	
//	public LoggerContextRemoteView(String name, Map<String, String> propertyMap) {
//		this.name = name;
//		this.propertyMap = propertyMap;
//	}
	
	public String getName() {
		return name;
	}
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

}
