package ch.qos.logback.core.model;

import java.util.Objects;

public class ParamModel extends NamedModel {

    private static final long serialVersionUID = -3697627721759508667L;
    String value;

    @Override
    protected ParamModel makeNewInstance() {
        return new ParamModel();
    }
    
    @Override
    protected void mirror(Model that) {
        ParamModel actual = (ParamModel) that;
        super.mirror(actual);
        this.value = actual.value;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParamModel other = (ParamModel) obj;
        return Objects.equals(value, other.value);
    }
    
}
