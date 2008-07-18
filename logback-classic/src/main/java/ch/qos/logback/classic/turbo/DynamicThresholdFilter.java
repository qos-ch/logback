package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.MDC;

import java.util.Map;
import java.util.HashMap;

/**
 * This filter will allow you to associate threshold levels to values found 
 * in the MDC. The threshold/value associations are looked up in MDC using
 * a key. This key can be any value specified by the user.  
 * 
 * <p>TO BE DISCUSSED...
 * 
 * <p>This provides very efficient course grained filtering based on things like a
 * product name or a company name that would be associated with requests as they
 * are being processed.
 * 
 * The example configuration below illustrates how debug logging could be
 * enabled for only individual users.
 * 
 * <pre>
 * &lt;turboFilter class=&quot;ch.qos.logback.classic.turbo.DynamicThresholdFilter&quot;&gt;
 *   &lt;Key&gt;userId&lt;/Key&gt;
 *   &lt;DefaultTheshold&gt;ERROR&lt;/DefaultTheshold&gt;
 *   &lt;MDCValueLevelPair&gt;
 *     &lt;value&gt;user1&lt;/value&gt;
 *     &lt;level&gt;DEBUG&lt;/level&gt;
 *   &lt;/MDCValueLevelPair&gt;
 *   &lt;MDCValueLevelPair&gt;
 *     &lt;value&gt;user2&lt;/value&gt;
 *     &lt;level&gt;TRACE&lt;/level&gt;
 *   &lt;/MDCValueLevelPair&gt;
 * &lt;/turboFilter&gt;
 * </pre>
 * 
 * @author Raplh Goers
 * @author Ceki Gulcu 
 */
public class DynamicThresholdFilter extends TurboFilter {
  private Map<String, Level> valueLevelMap = new HashMap<String, Level>();
  private Level defaultThreshold = Level.ERROR;
  private String key;

  /**
   * The MDC key that will be filtered against
   * 
   * @param key
   *                The name of the key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * 
   * @return The name of the key being filtered
   */
  public String getKey() {
    return this.key;
  }

  public Level getDefaultThreshold() {
    return defaultThreshold;
  }

  public void setDefaultThreshold(Level defaultThreshold) {
    this.defaultThreshold = defaultThreshold;
  }

  /**
   * Add a new MDCValuePair
   */
  public void addMDCValueLevelPair(MDCValueLevelPair mdcValueLevelPair) {
    if (valueLevelMap.containsKey(mdcValueLevelPair.getValue())) {
      addError(mdcValueLevelPair.getValue() + " has been already set");
    } else {
      valueLevelMap.put(mdcValueLevelPair.getValue(), mdcValueLevelPair
          .getLevel());
    }
  }

  /**
   * 
   */
  @Override
  public void start() {
    if (this.key == null) {
      addError("No key name was specified");
    }
    super.start();
  }

  /**
   * 
   * @param marker
   * @param logger
   * @param level
   * @param s
   * @param objects
   * @param throwable
   * @return
   */
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level,
      String s, Object[] objects, Throwable throwable) {
    String mdcValue = MDC.get(this.key);
    if(!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    Level levelAssociatedWithMDCValue = null;
    if (mdcValue != null) {
      levelAssociatedWithMDCValue = valueLevelMap.get(mdcValue);
    }
    if (levelAssociatedWithMDCValue == null) {
      levelAssociatedWithMDCValue = defaultThreshold;
    }
    if (level.isGreaterOrEqual(levelAssociatedWithMDCValue)) {
      return FilterReply.NEUTRAL;
    } else {
      return FilterReply.DENY;
    }
  }
}
