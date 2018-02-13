package ch.qos.logback.core.model;

public class Parameter {

    final String name;
    final String value;

    public Parameter(Model model) {
        name = model.tag;
        if(model.bodyText == null || model.bodyText.isEmpty())
          throw new IllegalArgumentException("Empty body for Parameter");
        value = model.bodyText.trim();
        if(value.isEmpty()) {
            throw new IllegalArgumentException("Empty body for Parameter"); 
        }
    }
    
    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Parameter [name=" + name + ", value=" + value + "]";
    }

}
