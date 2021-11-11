package ch.qos.logback.core.model;

public class TimestampModel extends NamedModel {

    /**
     *
     */
    private static final long serialVersionUID = 2096655273673863306L;

    public static final String CONTEXT_BIRTH = "contextBirth";

    String datePattern;
    String timeReference;
    String scopeStr;

    public String getKey() {
        return getName();
    }

    public void setKey(final String key) {
        setName(key);
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(final String datePattern) {
        this.datePattern = datePattern;
    }

    public String getTimeReference() {
        return timeReference;
    }

    public void setTimeReference(final String timeReference) {
        this.timeReference = timeReference;
    }

    public String getScopeStr() {
        return scopeStr;
    }

    public void setScopeStr(final String scopeStr) {
        this.scopeStr = scopeStr;
    }

}
