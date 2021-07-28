package ch.qos.logback.access.model;

import ch.qos.logback.core.model.Model;

public class ConfigurationModel extends Model {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5447825021342728679L;

	public static final String INTERNAL_DEBUG_ATTR = "debug";

	String debug;

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}
	
}
