/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.MD5Util;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ch.qos.logback.core.CoreConstants.PROPERTIES_FILE_EXTENSION;

/**
 * This class manages the list of files and/or urls that are watched for changes.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchList extends ContextAwareBase {

    public static final String HTTPS_PROTOCOL_STR = "https";
    public static final String HTTP_PROTOCOL_STR = "http";
    public static final String FILE_PROTOCOL_STR = "file";

    static final String[] WATCHABLE_PROTOCOLS = new String[] { FILE_PROTOCOL_STR, HTTPS_PROTOCOL_STR, HTTP_PROTOCOL_STR };

    static final byte[] BUF_ZERO = new byte[] { 0 };

    URL topURL;
    List<File> fileWatchList = new ArrayList<>();
    List<URL> urlWatchList = new ArrayList<>();
    List<byte[]> lastHashList = new ArrayList<>();

    List<Long> lastModifiedList = new ArrayList<>();

    public ConfigurationWatchList buildClone() {
        ConfigurationWatchList out = new ConfigurationWatchList();
        out.topURL = this.topURL;
        out.fileWatchList = new ArrayList<File>(this.fileWatchList);
        out.lastModifiedList = new ArrayList<Long>(this.lastModifiedList);
        out.lastHashList = new ArrayList<>(this.lastHashList);
        return out;
    }

    public void clear() {
        this.topURL = null;
        this.lastModifiedList.clear();
        this.fileWatchList.clear();
        this.urlWatchList.clear();
        this.lastHashList.clear();
    }

    /**
     * The topURL for the configuration file. Null values are allowed.
     *
     * @param topURL
     */
    public void setTopURL(URL topURL) {
        // topURL can be null
        this.topURL = topURL;
        if (topURL != null)
            addAsFileToWatch(topURL);
    }

    public boolean watchPredicateFulfilled() {
        if (hasMainURLAndNonEmptyFileList()) {
            return true;
        }

        if(urlListContainsProperties()) {
            return true;
        }

        return fileWatchListContainsProperties();

    }

    private boolean urlListContainsProperties() {
        return urlWatchList.stream().anyMatch(url -> url.toString().endsWith(PROPERTIES_FILE_EXTENSION));
    }

    private boolean hasMainURLAndNonEmptyFileList() {
        return topURL != null && !fileWatchList.isEmpty();
    }

    private boolean fileWatchListContainsProperties() {
        return fileWatchList.stream().anyMatch(file -> file.getName().endsWith(PROPERTIES_FILE_EXTENSION));

    }

    private void addAsFileToWatch(URL url) {
        File file = convertToFile(url);
        if (file != null) {
            fileWatchList.add(file);
            lastModifiedList.add(file.lastModified());
        }
    }


    private boolean isHTTP_Or_HTTPS(URL url) {
        String protocolStr = url.getProtocol();
        return isHTTP_Or_HTTPS(protocolStr);
    }

    private boolean isHTTP_Or_HTTPS(String protocolStr) {
        return (protocolStr.equals(HTTP_PROTOCOL_STR) || protocolStr.equals(HTTPS_PROTOCOL_STR));
    }

    private void addAsHTTP_or_HTTPS_URLToWatch(URL url) {
        if(isHTTP_Or_HTTPS(url)) {
            urlWatchList.add(url);
            lastHashList.add(BUF_ZERO);
        }
    }

    /**
     * Add the url but only if it is file:// or http(s)://
     * @param url should be a file or http(s)
     */
    public void addToWatchList(URL url) {
        // assume that the caller has checked that the protocol is one of {file, https, http}.
        String protocolStr = url.getProtocol();
        if (protocolStr.equals(FILE_PROTOCOL_STR)) {
            addAsFileToWatch(url);
        } else if (isHTTP_Or_HTTPS(protocolStr)) {
            addAsHTTP_or_HTTPS_URLToWatch(url);
        }
    }

    public URL getTopURL() {
        return topURL;
    }

    public List<File> getCopyOfFileWatchList() {
        return new ArrayList<File>(fileWatchList);
    }


    public boolean emptyWatchLists() {
        if(fileWatchList != null && !fileWatchList.isEmpty()) {
            return false;
        }

        if(urlWatchList != null && !urlWatchList.isEmpty()) {
            return false;
        }
        return true;
    }


    /**
     *
     * @deprecated replaced by {@link #changeDetectedInFile()}
     */
    public File changeDetected() {
      return changeDetectedInFile();
    }

    /**
     * Has a changed been detected in one of the files being watched?
     * @return
     */
    public File changeDetectedInFile() {
        int len = fileWatchList.size();

        for (int i = 0; i < len; i++) {
            long lastModified = lastModifiedList.get(i);
            File file = fileWatchList.get(i);
            long actualModificationDate = file.lastModified();

            if (lastModified != actualModificationDate) {
                // update modification date in case this instance is reused
                lastModifiedList.set(i, actualModificationDate);
                return file;
            }
        }
        return null;
    }

    public URL changeDetectedInURL() {
        int len = urlWatchList.size();

        for (int i = 0; i < len; i++) {
            byte[] lastHash = this.lastHashList.get(i);
            URL url = urlWatchList.get(i);

            HttpUtil httpGetUtil = new HttpUtil(HttpUtil.RequestMethod.GET, url);
            HttpURLConnection getConnection = httpGetUtil.connectTextTxt();
            String response = httpGetUtil.readResponse(getConnection);

            byte[] hash = computeHash(response);
            if (lastHash == BUF_ZERO) {
                this.lastHashList.set(i, hash);
                return null;
            }

            if (Arrays.equals(lastHash, hash)) {
                return null;
            } else {
                this.lastHashList.set(i, hash);
                return url;
            }
        }
        return null;
    }

    private byte[] computeHash(String response) {
        if (response == null || response.trim().length() == 0) {
            return null;
        }

        try {
            MD5Util md5Util = new MD5Util();
            byte[] hashBytes = md5Util.md5Hash(response);
            return hashBytes;
        } catch (NoSuchAlgorithmException e) {
            addError("missing MD5 algorithm", e);
            return null;
        }
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

    /**
     * Returns true if there are watchable files, false otherwise.
     * @return true if there are watchable files,  false otherwise.
     * @since 1.5.8
     */
    public boolean hasAtLeastOneWatchableFile() {
        return !fileWatchList.isEmpty();
    }

    /**
     * Is protocol for the given URL a protocol that we can watch for.
     *
     * @param url
     * @return true if watchable, false otherwise
     * @since 1.5.9
     */
    static public boolean isWatchableProtocol(URL url) {
        if (url == null) {
            return false;
        }
        String protocolStr = url.getProtocol();
        return isWatchableProtocol(protocolStr);
    }

    /**
     * Is the given protocol a protocol that we can watch for.
     *
     * @param protocolStr
     * @return true if watchable, false otherwise
     * @since 1.5.9
     */
    static public boolean isWatchableProtocol(String protocolStr) {
        return Arrays.stream(WATCHABLE_PROTOCOLS).anyMatch(protocol -> protocol.equalsIgnoreCase(protocolStr));
    }

    /**
     * Returns the urlWatchList field as a String
     * @return the urlWatchList field as a String
     * @since 1.5.19
     */
    public String getUrlWatchListAsStr() {
        String urlWatchListStr = urlWatchList.stream().map(URL::toString).collect(Collectors.joining(", "));
        return urlWatchListStr;
    }

    /**
     * Returns the fileWatchList field as a String
     * @return the fileWatchList field as a String
     * @since 1.5.19
     */
    public String getFileWatchListAsStr() {
        return fileWatchList.stream().map(File::getPath).collect(Collectors.joining(", "));
    }

}
