package ch.qos.logback.core.model;

public class NamedModel extends Model implements INamedModel {

    String name;

	@Override
    public String getName() {
		return name;
	}

	@Override
    public void setName(String name) {
		this.name = name;
	}
}
