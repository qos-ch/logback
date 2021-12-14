/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.spi.ContextAwareBase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchList extends ContextAwareBase {

    URL mainURL;
    Map<URL, Long> urlWatchMap = new HashMap<URL, Long>();

    public ConfigurationWatchList buildClone() {
        ConfigurationWatchList out = new ConfigurationWatchList();
        out.mainURL = this.mainURL;
        out.urlWatchMap = new HashMap<URL, Long>(this.urlWatchMap);
        return out;
    }

    public void clear() {
        this.mainURL = null;
        urlWatchMap.clear();
    }

    /**
     * The mainURL for the configuration file. Null values are allowed.
     *
     * @param mainURL
     */
    public void setMainURL(URL mainURL) {
        // main url can be null
        this.mainURL = mainURL;
        if (mainURL != null)
            addToWatchMap(mainURL);
    }

    private void addToWatchMap(URL url) {
        long lastModified = getLastModified(url);
        this.urlWatchMap.put(url, lastModified);
    }

    private long getLastModified(URL url) {
        URLConnection connection = null;
        try {
            connection = url.openConnection();
            return connection.getLastModified();
        } catch (IOException e) {
            addInfo("Failed to get Last Modified Date of: " + url, e);
        }
        return -1;
    }

    public void addToWatchList(URL url) {
        addToWatchMap(url);
    }

    public URL getMainURL() {
        return mainURL;
    }

    public List<URL> getCopyOfURLWatchList() {
        return new ArrayList<URL>(urlWatchMap.keySet());
    }

    public boolean changeDetected() {
        for (Map.Entry<URL, Long> entry : urlWatchMap.entrySet()) {
            if (entry.getValue() != getLastModified(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

}
