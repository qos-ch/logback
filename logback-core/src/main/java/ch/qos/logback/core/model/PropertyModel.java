package ch.qos.logback.core.model;

public class PropertyModel extends NamedModel {


	/**
	 *
	 */
	private static final long serialVersionUID = 1494176979175092052L;
	String value;
	String scopeStr;

	String file;
	String resource;


	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getScopeStr() {
		return scopeStr;
	}

	public void setScopeStr(final String scopeStr) {
		this.scopeStr = scopeStr;
	}

	public String getFile() {
		return file;
	}

	public void setFile(final String file) {
		this.file = file;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(final String resource) {
		this.resource = resource;
	}

}
