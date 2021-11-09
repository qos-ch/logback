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
package ch.qos.logback.core.joran.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;

public class House {
    Door mainDoor;
    int count;
    Double temperature;
    boolean open;
    String name;
    String camelCase;
    SwimmingPool pool;
    Duration duration;
    FileSize fs;
    HouseColor houseColor;
    FilterReply reply;

    Charset charset;

    List<String> adjectiveList = new ArrayList<>();
    List<Window> windowList = new ArrayList<>();
    List<SwimmingPool> largePoolList = new ArrayList<>();

    Orange orange;

    public String getCamelCase() {
        return camelCase;
    }

    public void setCamelCase(final String camelCase) {
        this.camelCase = camelCase;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int c) {
        count = c;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(final Double temperature) {
        this.temperature = temperature;
    }

    public Door getDoor() {
        return mainDoor;
    }

    public void setDoor(final Door door) {
        mainDoor = door;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(final boolean open) {
        this.open = open;
    }

    @DefaultClass(LargeSwimmingPoolImpl.class)
    public void addLargeSwimmingPool(final SwimmingPool pool) {
        this.pool = pool;
    }

    @DefaultClass(SwimmingPoolImpl.class)
    public void setSwimmingPool(final SwimmingPool pool) {
        this.pool = pool;
    }

    public SwimmingPool getSwimmingPool() {
        return pool;
    }

    public void addWindow(final Window w) {
        windowList.add(w);
    }

    public void addAdjective(final String s) {
        adjectiveList.add(s);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(final Duration duration) {
        this.duration = duration;
    }

    public FileSize getFs() {
        return fs;
    }

    public void setFs(final FileSize fs) {
        this.fs = fs;
    }

    public void setHouseColor(final HouseColor color) {
        houseColor = color;
    }

    public HouseColor getHouseColor() {
        return houseColor;
    }

    public void setFilterReply(final FilterReply reply) {
        this.reply = reply;
    }

    public FilterReply getFilterReply() {
        return reply;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    public void setOrange(final Orange o) {
        orange = o;
    }
}

class Door {
    int handle;
}

interface SwimmingPool {
}

class SwimmingPoolImpl implements SwimmingPool {
    int length;
    int width;
    int depth;
}

class LargeSwimmingPoolImpl implements SwimmingPool {
    int length;
    int width;
    int depth;
}

enum HouseColor {
    WHITE, BLUE
}
