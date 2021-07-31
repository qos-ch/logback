package ch.qos.logback.core.model;

public class NamedComponentModel extends ComponentModel implements INamedModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6388316680413871442L;
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
