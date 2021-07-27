package ch.qos.logback.core.model;

public class AppenderModel extends NamedComponentModel {

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
}
