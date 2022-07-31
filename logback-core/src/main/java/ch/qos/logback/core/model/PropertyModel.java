package ch.qos.logback.core.model;

import java.util.Objects;

public class PropertyModel extends NamedModel {

    private static final long serialVersionUID = 1494176979175092052L;
    
    String value;
    String scopeStr;

    String file;
    String resource;

    @Override
    protected PropertyModel makeNewInstance() {
        return new PropertyModel();
    }
    
    @Override
    protected void mirror(Model that) {
        PropertyModel actual = (PropertyModel) that;
        super.mirror(actual);
        this.value = actual.value;
        this.scopeStr = actual.scopeStr;
        this.file = actual.file;
        this.resource = actual.resource;
        
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getScopeStr() {
        return scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(file, resource, scopeStr, value);
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
        PropertyModel other = (PropertyModel) obj;
        return Objects.equals(file, other.file) && Objects.equals(resource, other.resource)
                && Objects.equals(scopeStr, other.scopeStr) && Objects.equals(value, other.value);
    }

    
}
