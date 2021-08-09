package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;

public class LevelModel extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7287549849308062148L;
	String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	
}
