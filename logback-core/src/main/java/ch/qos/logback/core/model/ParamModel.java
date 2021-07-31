package ch.qos.logback.core.model;

public class ParamModel extends NamedModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3697627721759508667L;
	String value;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
