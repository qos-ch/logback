package ch.qos.logback.classic.model;

import java.util.Objects;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.Duration;

public class ConfigurationModel extends Model {

    private static final long serialVersionUID = 1286156598561818515L;

    String debugStr;
    String scanStr;
    String scanPeriodStr;
    String packagingDataStr;
    
    @Override
    protected ConfigurationModel makeNewInstance() {
        return new ConfigurationModel();
    }
    
    @Override protected void mirror(Model that) {
        ConfigurationModel actual = (ConfigurationModel) that;
        super.mirror(that);
        this.debugStr = actual.debugStr;
        this.scanStr = actual.scanStr;
        this.scanPeriodStr = actual.scanPeriodStr;
        this.packagingDataStr = actual.packagingDataStr;
    }
    
    public String getDebugStr() {
        return debugStr;
    }

    public void setDebugStr(String debugStr) {
        this.debugStr = debugStr;
    }

    public String getScanStr() {
        return scanStr;
    }

    public void setScanStr(String scanStr) {
        this.scanStr = scanStr;
    }

    public String getScanPeriodStr() {
        return scanPeriodStr;
    }

    public void setScanPeriodStr(String scanPeriodStr) {
        this.scanPeriodStr = scanPeriodStr;
    }

    public String getPackagingDataStr() {
        return packagingDataStr;
    }

    public void setPackagingDataStr(String packagingDataStr) {
        this.packagingDataStr = packagingDataStr;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(debugStr, packagingDataStr, scanPeriodStr, scanStr);
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
        ConfigurationModel other = (ConfigurationModel) obj;
        return Objects.equals(debugStr, other.debugStr) && Objects.equals(packagingDataStr, other.packagingDataStr)
                && Objects.equals(scanPeriodStr, other.scanPeriodStr) && Objects.equals(scanStr, other.scanStr);
    }

}
