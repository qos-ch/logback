package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;

public class LoggerModel extends Model {
 
    String name;
    String level;
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
}
