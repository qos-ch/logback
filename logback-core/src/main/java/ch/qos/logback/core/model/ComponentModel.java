package ch.qos.logback.core.model;

/**
 * Abstract representation of configuration elements
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class ComponentModel extends Model {

    String className;
    
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +" [tag=" + tag + ", className=" + className + ", bodyText=" + bodyText + "]";
    }

}