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
package ch.qos.logback.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instances of this class represent the size of a file. Internally, the size is
 * stored as long.
 * 
 * <p>The {@link #valueOf} method can convert strings such as "3 kb", "5 mb", into
 * FileSize instances. The recognized unit specifications for file size are the
 * "kb", "mb", and "gb". The unit name may be followed by an "s". Thus, "2 kbs"
 * and "2 kb" are equivalent. In the absence of a time unit specification, byte
 * is assumed.
 *  
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class FileSize {

    private final static String LENGTH_PART = "([0-9]+)";
    private final static int DOUBLE_GROUP = 1;

    private final static String UNIT_PART = "(|kb|mb|gb)s?";
    private final static int UNIT_GROUP = 2;

    private static final Pattern FILE_SIZE_PATTERN = Pattern.compile(LENGTH_PART + "\\s*" + UNIT_PART, Pattern.CASE_INSENSITIVE);

    static public final long KB_COEFFICIENT = 1024;
    static public final long MB_COEFFICIENT = 1024 * KB_COEFFICIENT;
    static public final long GB_COEFFICIENT = 1024 * MB_COEFFICIENT;

    final long size;

    public FileSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    static public FileSize valueOf(String fileSizeStr) {
        Matcher matcher = FILE_SIZE_PATTERN.matcher(fileSizeStr);

        long coefficient;
        if (matcher.matches()) {
            String lenStr = matcher.group(DOUBLE_GROUP);
            String unitStr = matcher.group(UNIT_GROUP);

            long lenValue = Long.valueOf(lenStr);
            if (unitStr.equalsIgnoreCase("")) {
                coefficient = 1;
            } else if (unitStr.equalsIgnoreCase("kb")) {
                coefficient = KB_COEFFICIENT;
            } else if (unitStr.equalsIgnoreCase("mb")) {
                coefficient = MB_COEFFICIENT;
            } else if (unitStr.equalsIgnoreCase("gb")) {
                coefficient = GB_COEFFICIENT;
            } else {
                throw new IllegalStateException("Unexpected " + unitStr);
            }
            return new FileSize(lenValue * coefficient);
        } else {
            throw new IllegalArgumentException("String value [" + fileSizeStr + "] is not in the expected format.");
        }
    }
    
    @Override
    public String toString() {
        long inKB = size / KB_COEFFICIENT;
        
        if(inKB == 0) 
            return size + " Bytes";
        
        long inMB = size / MB_COEFFICIENT;
        if(inMB == 0) {
            return inKB + " KB";
        }
        
        long inGB = size / GB_COEFFICIENT;
        if(inGB == 0) {
            return inMB + " MB";
        }
        
        return inGB + " GB";
        
    }
}
