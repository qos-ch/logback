package ch.qos.logback.core.model;

public class NamedComponentModel extends ComponentModel implements INamedModel {

	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
