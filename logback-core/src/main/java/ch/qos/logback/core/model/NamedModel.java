package ch.qos.logback.core.model;

public class NamedModel extends Model implements INamedModel {

    String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
