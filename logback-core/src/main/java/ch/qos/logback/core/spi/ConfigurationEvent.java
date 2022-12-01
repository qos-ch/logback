/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.spi;

/**
 * This class configuration events which can be of various types such as
 *  CHANGE_DETECTED, CONFIGURATION_STARTED and CONFIGURATION_ENDED.
 *
 *  Configuration events can be accompanied by supplemental data which can be null.
 *
 * @since 1.3.6/1.4.6
 */

public class ConfigurationEvent {


    public enum EventType {
        CHANGE_DETECTOR_REGISTERED,

        CHANGE_DETECTOR_RUNNING,
        CHANGE_DETECTED,
        CONFIGURATION_STARTED,
        CONFIGURATION_ENDED;
    }
    final EventType eventType;
    final Object data;

    /**
     * Construct a ConfigurationEvent instance.
     *
     * @param eventType
     * @param data supplemental data, can be null
     */
    private ConfigurationEvent(EventType eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    static public ConfigurationEvent newConfigurationChangeDetectorRunningEvent(Object data) {
        return new ConfigurationEvent(EventType.CHANGE_DETECTOR_RUNNING, data);
    }

    static public ConfigurationEvent newConfigurationChangeDetectorRegisteredEvent(Object data) {
        return new ConfigurationEvent(EventType.CHANGE_DETECTOR_REGISTERED, data);
    }
    static public ConfigurationEvent newConfigurationChangeDetectedEvent(Object data) {
        return new ConfigurationEvent(EventType.CHANGE_DETECTED, data);
    }
    static public ConfigurationEvent newConfigurationStartedEvent(Object data) {
        return new ConfigurationEvent(EventType.CONFIGURATION_STARTED, data);
    }
    static public ConfigurationEvent newConfigurationEndedEvent(Object data) {
        return new ConfigurationEvent(EventType.CONFIGURATION_ENDED, data);
    }

    public EventType getEventType() {
        return eventType;
    }

    public Object getData() {
        return data;
    }


    @Override
    public String toString() {
        return "ConfigurationEvent{" + "eventType=" + eventType + ", data=" + data + '}';
    }
}
