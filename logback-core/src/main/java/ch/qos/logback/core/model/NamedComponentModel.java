package ch.qos.logback.core.model;

public class NamedComponentModel extends ComponentModel implements INamedModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6388316680413871442L;
	String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}
}
