package ch.qos.logback.core.joran.util;

public abstract class Citrus<T> {

    public static final String PRECARP_PROPERTY_NAME = "pericarp";
    public static final String PREFIX_PROPERTY_NAME = "prefix";
    
    @SuppressWarnings("unused")
    private T pericarp;
    
    String prefix;
    
    public void setPericarp(T pericarp) {
        this.pericarp = pericarp;
    }
       
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public abstract void foo();
    
    
    
}
