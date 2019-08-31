package ch.qos.logback.core.model;

import ch.qos.logback.core.joran.spi.ElementPath;

public class IncludeModel extends Model {

	String optional;
	String file;
	String url;
	String resource;
	ElementPath elementPath;
	


	public String getOptional() {
		return optional;
	}

	public void setOptional(String optional) {
		this.optional = optional;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public ElementPath getElementPath() {
		return elementPath;
	}

	public void setElementPath(ElementPath elementPath) {
		this.elementPath = elementPath;
	}


}
