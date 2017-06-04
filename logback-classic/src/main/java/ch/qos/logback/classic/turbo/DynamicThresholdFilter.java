/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.MDC;

import java.util.Map;
import java.util.HashMap;

/**
 * This filter allows for efficient course grained filtering based on criteria
 * such as product name or company name that would be associated with requests
 * as they are processed.
 * 
 * <p> This filter will allow you to associate threshold levels to a key put in
 * the MDC. This key can be any value specified by the user. Furthermore, you
 * can pass MDC value and level threshold associations, which are then looked up
 * to find the level threshold to apply to the current logging request. If no
 * level threshold could be found, then a 'default' value specified by the user
 * is applied. We call this value 'levelAssociatedWithMDCValue'.
 * 
 * <p> If 'levelAssociatedWithMDCValue' is higher or equal to the level of the
 * current logger request, the
 * {@link #decide(Marker, Logger, Level, String, Object[], Throwable) decide()}
 * method returns the value of {@link #getOnHigherOrEqual() onHigherOrEqual},
 * if it is lower then the value of {@link #getOnLower() onLower} is returned.
 * Both 'onHigherOrEqual' and 'onLower' can be set by the user. By default,
 * 'onHigherOrEqual' is set to NEUTRAL and 'onLower' is set to DENY. Thus, if
 * the current logger request's level is lower than
 * 'levelAssociatedWithMDCValue', then the request is denied, and if it is
 * higher or equal, then this filter decides NEUTRAL letting subsequent filters
 * to make the decision on the fate of the logging request.
 * 
 * <p> The example below illustrates how logging could be enabled for only
 * individual users. In this example all events for logger names matching
 * "com.mycompany" will be logged if they are for 'user1' and at a level higher
 * than equals to DEBUG, and for 'user2' if they are at a level higher than or
 * equal to TRACE, and for other users only if they are at level ERROR or
 * higher. Events issued by loggers other than "com.mycompany" will only be
 * logged if they are at level ERROR or higher since that is all the root logger
 * allows.
 * 
 * <pre>
 * &lt;configuration&gt;
 *   &lt;appender name="STDOUT"
 *             class="ch.qos.logback.core.ConsoleAppender"&gt;
 *     &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *       &lt;Pattern>TEST %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n&lt;/Pattern>
 *     &lt;/layout&gt;
 *   &lt;/appender&gt;
 *   
 *   &lt;turboFilter class=&quot;ch.qos.logback.classic.turbo.DynamicThresholdFilter&quot;&gt;
 *     &lt;Key&gt;userId&lt;/Key&gt;
 *     &lt;DefaultThreshold&gt;ERROR&lt;/DefaultThreshold&gt;
 *     &lt;MDCValueLevelPair&gt;
 *       &lt;value&gt;user1&lt;/value&gt;
 *       &lt;level&gt;DEBUG&lt;/level&gt;
 *     &lt;/MDCValueLevelPair&gt;
 *     &lt;MDCValueLevelPair&gt;
 *       &lt;value&gt;user2&lt;/value&gt;
 *       &lt;level&gt;TRACE&lt;/level&gt;
 *     &lt;/MDCValueLevelPair&gt;
 *   &lt;/turboFilter&gt;
 *   
 *   &lt;logger name="com.mycompany" level="TRACE"/&gt;
 *   
 *   &lt;root level="ERROR" &gt;
 *     &lt;appender-ref ref="STDOUT" /&gt;
 *   &lt;/root&gt;
 * &lt;/configuration&gt;
 * </pre>
 * 
 * In the next configuration events from user1 and user2 will be logged
 * regardless of the logger levels. Events for other users and records without a
 * userid in the MDC will be logged if they are ERROR level messages. With this
 * configuration, the root level is never checked since DynamicThresholdFilter
 * will either accept or deny all records.
 * 
 * <pre>
 * &lt;configuration&gt;
 *   &lt;appender name="STDOUT"
 *             class="ch.qos.logback.core.ConsoleAppender"&gt;
 *     &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *        &lt;Pattern>TEST %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n&lt;/Pattern>
 *     &lt;/layout&gt;
 *   &lt;/appender&gt;
 *   
 *   &lt;turboFilter class=&quot;ch.qos.logback.classic.turbo.DynamicThresholdFilter&quot;&gt;
 *     &lt;Key&gt;userId&lt;/Key&gt;
 *     &lt;DefaultThreshold&gt;ERROR&lt;/DefaultThreshold&gt;
 *     &lt;OnHigherOrEqual&gt;ACCEPT&lt;/OnHigherOrEqual&gt;
 *     &lt;OnLower&gt;DENY&lt;/OnLower&gt;
 *     &lt;MDCValueLevelPair&gt;
 *       &lt;value&gt;user1&lt;/value&gt;
 *       &lt;level&gt;TRACE&lt;/level&gt;
 *     &lt;/MDCValueLevelPair&gt;
 *     &lt;MDCValueLevelPair&gt;
 *       &lt;value&gt;user2&lt;/value&gt;
 *       &lt;level&gt;TRACE&lt;/level&gt;
 *     &lt;/MDCValueLevelPair&gt;
 *   &lt;/turboFilter&gt;
 *   
 *   &lt;root level="DEBUG" &gt;
 *     &lt;appender-ref ref="STDOUT" /&gt;
 *   &lt;/root&gt;
 * &lt;/configuration&gt;
 * </pre>
 * 
 * @author Ralph Goers
 * @author Ceki G&uuml;lc&uuml;
 */
public class DynamicThresholdFilter extends TurboFilter {
    private Map<String, Level> valueLevelMap = new HashMap<String, Level>();
    private Level defaultThreshold = Level.ERROR;
    private String key;

    private FilterReply onHigherOrEqual = FilterReply.NEUTRAL;
    private FilterReply onLower = FilterReply.DENY;

    /**
     * Get the MDC key whose value will be used as a level threshold
     * 
     * @return the name of the MDC key.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @see setKey
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get the default threshold value when the MDC key is not set.
     * 
     * @return the default threshold value in the absence of a set MDC key
     */
    public Level getDefaultThreshold() {
        return defaultThreshold;
    }

    public void setDefaultThreshold(Level defaultThreshold) {
        this.defaultThreshold = defaultThreshold;
    }

    /**
     * Get the FilterReply when the effective level is higher or equal to the
     * level of current logging request
     * 
     * @return FilterReply
     */
    public FilterReply getOnHigherOrEqual() {
        return onHigherOrEqual;
    }

    public void setOnHigherOrEqual(FilterReply onHigherOrEqual) {
        this.onHigherOrEqual = onHigherOrEqual;
    }

    /**
     * Get the FilterReply when the effective level is lower than the level of
     * current logging request
     * 
     * @return FilterReply
     */
    public FilterReply getOnLower() {
        return onLower;
    }

    public void setOnLower(FilterReply onLower) {
        this.onLower = onLower;
    }

    /**
     * Add a new MDCValuePair
     */
    public void addMDCValueLevelPair(MDCValueLevelPair mdcValueLevelPair) {
        if (valueLevelMap.containsKey(mdcValueLevelPair.getValue())) {
            addError(mdcValueLevelPair.getValue() + " has been already set");
        } else {
            valueLevelMap.put(mdcValueLevelPair.getValue(), mdcValueLevelPair.getLevel());
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
     * This method first finds the MDC value for 'key'. It then finds the level
     * threshold associated with this MDC value from the list of MDCValueLevelPair
     * passed to this filter. This value is stored in a variable called
     * 'levelAssociatedWithMDCValue'. If it null, then it is set to the
     * 
     * @{link #defaultThreshold} value.
     * 
     * If no such value exists, then
     * 
     * 
     * @param marker
     * @param logger
     * @param level
     * @param s
     * @param objects
     * @param throwable
     * 
     * @return FilterReply - this filter's decision
     */
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable) {

        String mdcValue = MDC.get(this.key);
        if (!isStarted()) {
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
            return onHigherOrEqual;
        } else {
            return onLower;
        }
    }
}
