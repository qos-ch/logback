package ch.qos.logback.core.model;

public class DefineModel extends ComponentModel {

    String name;
    String scopeStr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScopeStr() {
        return scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }
    
    
}
