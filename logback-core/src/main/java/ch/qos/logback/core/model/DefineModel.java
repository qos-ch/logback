package ch.qos.logback.core.model;

public class DefineModel extends NamedComponentModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6209642548924431065L;
	String scopeStr;

    public String getScopeStr() {
        return scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }
    
    
}
