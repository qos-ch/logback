package ch.qos.logback.core.model;

public class ImportModel extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Class/getClass() are part of java.lang.Object. We use 'className' instead.
	String className;

	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}
	
	
}
