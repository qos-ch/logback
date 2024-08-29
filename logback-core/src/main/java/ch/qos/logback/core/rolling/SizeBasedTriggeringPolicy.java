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
package ch.qos.logback.core.rolling;

import java.io.File;

import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.DefaultInvocationGate;
import ch.qos.logback.core.util.InvocationGate;
import ch.qos.logback.core.util.SimpleInvocationGate;

/**
 * SizeBasedTriggeringPolicy looks at size of the file being currently written
 * to. If it grows bigger than the specified size, the FileAppender using the
 * SizeBasedTriggeringPolicy rolls the file and creates a new one.
 * 
 * For more information about this policy, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#SizeBasedTriggeringPolicy
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class SizeBasedTriggeringPolicy<E> extends TriggeringPolicyBase<E> {

    public static final String SEE_SIZE_FORMAT = "http://logback.qos.ch/codes.html#sbtp_size_format";
    /**
     * The default maximum file size.
     */
    public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    FileSize maxFileSize = new FileSize(DEFAULT_MAX_FILE_SIZE);
    InvocationGate invocationGate = new SimpleInvocationGate();
    Duration checkIncrement = null;

    public SizeBasedTriggeringPolicy() {
    }

    public void start() {
        if(checkIncrement != null)
            invocationGate = new SimpleInvocationGate(checkIncrement);
        super.start();
    }


    @Override
    public boolean isTriggeringEvent(File activeFile, E event, long currentFilePosition) {
        long now = System.currentTimeMillis();
        if (invocationGate.isTooSoon(now))
            return false;
        long activeFileLength = currentFilePosition >= 0 ? currentFilePosition : activeFile.length();
        return (activeFileLength >= maxFileSize.getSize());
    }

    public boolean isTriggeringEvent(final File activeFile, final E event) {
        return isTriggeringEvent(activeFile, event, -1);
    }

    public FileSize getMaxFileSize() {
        return this.maxFileSize;
    }

    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }

    public Duration getCheckIncrement() {
        return checkIncrement;
    }

    public void setCheckIncrement(Duration checkIncrement) {
        this.checkIncrement = checkIncrement;
    }
}
