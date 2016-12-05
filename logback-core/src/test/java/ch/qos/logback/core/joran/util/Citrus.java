package ch.qos.logback.core.joran.util;

public class Citrus<T> {

    // should match name of the single property of this class
    public static final String PRECARP_PROPERTY_NAME = "pericarp";
    @SuppressWarnings("unused")
    private T pericarp;
    
    public void setPericarp(T pericarp) {
        this.pericarp = pericarp;
    }
    
    
}
