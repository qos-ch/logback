package ch.qos.logback.core.model;

public class TimestampModel extends Model {

    public static final String CONTEXT_BIRTH = "contextBirth";

    
    String key;
    String datePattern;
    String timeReference;
    String scopeStr;
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getDatePattern() {
        return datePattern;
    }
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
    public String getTimeReference() {
        return timeReference;
    }
    public void setTimeReference(String timeReference) {
        this.timeReference = timeReference;
    }
    public String getScopeStr() {
        return scopeStr;
    }
    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }
    
    
}
