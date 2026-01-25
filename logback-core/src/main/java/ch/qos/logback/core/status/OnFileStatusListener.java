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

package ch.qos.logback.core.status;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class OnFileStatusListener extends OnPrintStreamStatusListenerBase {

    String filename;
    PrintStream ps;

    @Override
    public void start() {
        if (filename == null) {
            addInfo("File option not set. Defaulting to \"status.txt\"");
            filename = "status.txt";
        }

        try {
            FileOutputStream fos = new FileOutputStream(filename, true);
            ps = new PrintStream(fos, true);
        } catch (FileNotFoundException e) {
            addError("Failed to open [" + filename + "]", e);
            return;
        }

        super.start();

    }

    @Override
    public void stop() {
        if (!isStarted) {
            return;
        }
        if (ps != null)
            ps.close();
        super.stop();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    protected PrintStream getPrintStream() {
        return ps;
    }

}
