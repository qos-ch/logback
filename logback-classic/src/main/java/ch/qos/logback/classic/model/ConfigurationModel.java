package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.Duration;

public class ConfigurationModel extends Model {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1286156598561818515L;
	static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback.debug";
    static final Duration SCAN_PERIOD_DEFAULT = Duration.buildByMinutes(1);
 
    
    String debugStr;
    String scanStr;
    String scanPeriodStr;
    String packagingDataStr;
    

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
}
