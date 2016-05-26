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
package ch.qos.logback.core.sift;

import java.util.List;
import java.util.Map;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Builds new appenders dynamically by running SiftingJoranConfigurator instance,
 * a custom configurator tailored for the contents of the sift element.
 * @param <E>
 */
public abstract class AbstractAppenderFactoryUsingJoran<E> implements AppenderFactory<E> {

    final List<SaxEvent> eventList;
    protected String key;
    protected Map<String, String> parentPropertyMap;

    protected AbstractAppenderFactoryUsingJoran(List<SaxEvent> eventList, String key, Map<String, String> parentPropertyMap) {
        this.eventList = removeSiftElement(eventList);
        this.key = key;
        this.parentPropertyMap = parentPropertyMap;

    }

    List<SaxEvent> removeSiftElement(List<SaxEvent> eventList) {
        return eventList.subList(1, eventList.size() - 1);
    }

    public abstract SiftingJoranConfiguratorBase<E> getSiftingJoranConfigurator(String k);

    public Appender<E> buildAppender(Context context, String discriminatingValue) throws JoranException {
        SiftingJoranConfiguratorBase<E> sjc = getSiftingJoranConfigurator(discriminatingValue);
        sjc.setContext(context);
        sjc.doConfigure(eventList);
        return sjc.getAppender();
    }

    public List<SaxEvent> getEventList() {
        return eventList;
    }

}
