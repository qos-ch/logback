package ch.qos.logback.core.model;

public class AppenderRefModel extends Model {

	String ref;

	@Override
    public boolean isUnhandled() {
    	return super.isUnhandled();
	}
	@Override
    public boolean isHandled() {
		return super.isHandled();
	}   
	
	@Override 
	public void markAsHandled() {
		super.markAsHandled();
	}
	
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
	
	
}
