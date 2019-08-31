package ch.qos.logback.access.model;

import ch.qos.logback.core.model.Model;

public class ConfigurationModel extends Model {
    public static final String INTERNAL_DEBUG_ATTR = "debug";

	String debug;

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}
	
}
