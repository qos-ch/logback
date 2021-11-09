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
package ch.qos.logback.core.recovery;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

abstract public class ResilientOutputStreamBase extends OutputStream {

    final static int STATUS_COUNT_LIMIT = 2 * 4;

    private int noContextWarning = 0;
    private int statusCount = 0;

    private Context context;
    private RecoveryCoordinator recoveryCoordinator;

    protected OutputStream os;
    protected boolean presumedClean = true;

    private boolean isPresumedInError() {
        // existence of recoveryCoordinator indicates failed state
        return recoveryCoordinator != null && !presumedClean;
    }

    @Override
    public void write(final byte b[], final int off, final int len) {
        if (isPresumedInError()) {
            if (!recoveryCoordinator.isTooSoon()) {
                attemptRecovery();
            }
            return; // return regardless of the success of the recovery attempt
        }

        try {
            os.write(b, off, len);
            postSuccessfulWrite();
        } catch (final IOException e) {
            postIOFailure(e);
        }
    }

    @Override
    public void write(final int b) {
        if (isPresumedInError()) {
            if (!recoveryCoordinator.isTooSoon()) {
                attemptRecovery();
            }
            return; // return regardless of the success of the recovery attempt
        }
        try {
            os.write(b);
            postSuccessfulWrite();
        } catch (final IOException e) {
            postIOFailure(e);
        }
    }

    @Override
    public void flush() {
        if (os != null) {
            try {
                os.flush();
                postSuccessfulWrite();
            } catch (final IOException e) {
                postIOFailure(e);
            }
        }
    }

    abstract String getDescription();

    abstract OutputStream openNewOutputStream() throws IOException;

    private void postSuccessfulWrite() {
        if (recoveryCoordinator != null) {
            recoveryCoordinator = null;
            statusCount = 0;
            addStatus(new InfoStatus("Recovered from IO failure on " + getDescription(), this));
        }
    }

    public void postIOFailure(final IOException e) {
        addStatusIfCountNotOverLimit(new ErrorStatus("IO failure while writing to " + getDescription(), this, e));
        presumedClean = false;
        if (recoveryCoordinator == null) {
            recoveryCoordinator = new RecoveryCoordinator();
        }
    }

    @Override
    public void close() throws IOException {
        if (os != null) {
            os.close();
        }
    }

    void attemptRecovery() {
        try {
            close();
        } catch (final IOException e) {
        }

        addStatusIfCountNotOverLimit(new InfoStatus("Attempting to recover from IO failure on " + getDescription(), this));

        // subsequent writes must always be in append mode
        try {
            os = openNewOutputStream();
            presumedClean = true;
        } catch (final IOException e) {
            addStatusIfCountNotOverLimit(new ErrorStatus("Failed to open " + getDescription(), this, e));
        }
    }

    void addStatusIfCountNotOverLimit(final Status s) {
        ++statusCount;
        if (statusCount < STATUS_COUNT_LIMIT) {
            addStatus(s);
        }

        if (statusCount == STATUS_COUNT_LIMIT) {
            addStatus(s);
            addStatus(new InfoStatus("Will supress future messages regarding " + getDescription(), this));
        }
    }

    public void addStatus(final Status status) {
        if (context == null) {
            if (noContextWarning++ == 0) {
                System.out.println("LOGBACK: No context given for " + this);
            }
            return;
        }
        final StatusManager sm = context.getStatusManager();
        if (sm != null) {
            sm.add(status);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }
}
