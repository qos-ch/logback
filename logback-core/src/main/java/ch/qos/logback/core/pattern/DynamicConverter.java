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
package ch.qos.logback.core.pattern;

import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.Status;

abstract public class DynamicConverter<E> extends FormattingConverter<E> implements LifeCycle, ContextAware {

    ContextAwareBase cab = new ContextAwareBase(this);

    // Contains a list of option Strings.
    private List<String> optionList;

    /**
     * Is this component active?
     */
    protected boolean started = false;

    /**
     * Components that depend on options passed during configuration can override
     * this method in order to make appropriate use of those options. For simpler
     * components, the trivial implementation found in this abstract class will be
     * sufficient.
     */
    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public void setOptionList(List<String> optionList) {
        this.optionList = optionList;
    }

    /**
     * Return the first option passed to this component. The returned value may be
     * null if there are no options.
     * 
     * @return First option, may be null.
     */
    public String getFirstOption() {
        if (optionList == null || optionList.size() == 0) {
            return null;
        } else {
            return optionList.get(0);
        }
    }

    protected List<String> getOptionList() {
        return optionList;
    }

    @Override
    public void setContext(Context context) {
        cab.setContext(context);
    }

    @Override
    public Context getContext() {
        return cab.getContext();
    }

    @Override
    public void addStatus(Status status) {
        cab.addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        cab.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        cab.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        cab.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        cab.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        cab.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        cab.addError(msg, ex);
    }
}
