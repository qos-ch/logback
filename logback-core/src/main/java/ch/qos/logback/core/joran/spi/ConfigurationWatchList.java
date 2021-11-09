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
package ch.qos.logback.core.joran.spi;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchList extends ContextAwareBase {

    URL mainURL;
    List<File> fileWatchList = new ArrayList<>();
    List<Long> lastModifiedList = new ArrayList<>();

    public ConfigurationWatchList buildClone() {
        final ConfigurationWatchList out = new ConfigurationWatchList();
        out.mainURL = mainURL;
        out.fileWatchList = new ArrayList<>(fileWatchList);
        out.lastModifiedList = new ArrayList<>(lastModifiedList);
        return out;
    }

    public void clear() {
        mainURL = null;
        lastModifiedList.clear();
        fileWatchList.clear();
    }

    /**
     * The mainURL for the configuration file. Null values are allowed.
     * @param mainURL
     */
    public void setMainURL(final URL mainURL) {
        // main url can be null
        this.mainURL = mainURL;
        if (mainURL != null) {
            addAsFileToWatch(mainURL);
        }
    }

    private void addAsFileToWatch(final URL url) {
        final File file = convertToFile(url);
        if (file != null) {
            fileWatchList.add(file);
            lastModifiedList.add(file.lastModified());
        }
    }

    public void addToWatchList(final URL url) {
        addAsFileToWatch(url);
    }

    public URL getMainURL() {
        return mainURL;
    }

    public List<File> getCopyOfFileWatchList() {
        return new ArrayList<>(fileWatchList);
    }

    public boolean changeDetected() {
        final int len = fileWatchList.size();
        for (int i = 0; i < len; i++) {
            final long lastModified = lastModifiedList.get(i);
            final File file = fileWatchList.get(i);
            if (lastModified != file.lastModified()) {
                return true;
            }
        }
        return false;
        // return (lastModified != fileToScan.lastModified() && lastModified != SENTINEL);
    }

    @SuppressWarnings("deprecation")
    File convertToFile(final URL url) {
        final String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            return new File(URLDecoder.decode(url.getFile()));
        }
        addInfo("URL [" + url + "] is not of type file");
        return null;
    }

}
