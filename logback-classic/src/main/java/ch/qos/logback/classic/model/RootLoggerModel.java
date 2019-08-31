package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;

public class RootLoggerModel extends Model {
 
    String level;
    
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
}
