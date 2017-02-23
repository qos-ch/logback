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

import ch.qos.logback.core.spi.ContextAwareBase;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchList extends ContextAwareBase {

    URL mainURL;
    List<File> fileWatchList = new ArrayList<File>();
    List<Long> lastModifiedList = new ArrayList<Long>();

    public ConfigurationWatchList buildClone() {
        ConfigurationWatchList out = new ConfigurationWatchList();
        out.mainURL = this.mainURL;
        out.fileWatchList = new ArrayList<File>(this.fileWatchList);
        out.lastModifiedList = new ArrayList<Long>(this.lastModifiedList);
        return out;
    }
    
    public void clear() {
        this.mainURL = null;
        lastModifiedList.clear();
        fileWatchList.clear();
    }

    /**
     * The mainURL for the configuration file. Null values are allowed.
     * @param mainURL
     */
    public void setMainURL(URL mainURL) {
        // main url can be null
        this.mainURL = mainURL;
        if (mainURL != null)
            addAsFileToWatch(mainURL);
    }

    private void addAsFileToWatch(URL url) {
        File file = convertToFile(url);
        if (file != null) {
            fileWatchList.add(file);
            lastModifiedList.add(file.lastModified());
        }
    }

    public void addToWatchList(URL url) {
        addAsFileToWatch(url);
    }

    public URL getMainURL() {
        return mainURL;
    }

    public List<File> getCopyOfFileWatchList() {
        return new ArrayList<File>(fileWatchList);
    }

    public boolean changeDetected() {
        int len = fileWatchList.size();
        for (int i = 0; i < len; i++) {
            long lastModified = lastModifiedList.get(i);
            File file = fileWatchList.get(i);
            if (lastModified != file.lastModified()) {
                return true;
            }
        }
        return false;
        // return (lastModified != fileToScan.lastModified() && lastModified != SENTINEL);
    }

    @SuppressWarnings("deprecation")
    File convertToFile(URL url) {
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            return new File(URLDecoder.decode(url.getFile()));
        } else {
            addInfo("URL [" + url + "] is not of type file");
            return null;
        }
    }

}
