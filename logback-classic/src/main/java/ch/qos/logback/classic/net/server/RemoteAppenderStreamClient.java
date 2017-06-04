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
package ch.qos.logback.classic.net.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.net.HardenedObjectInputStream;
import ch.qos.logback.core.util.CloseUtil;

/**
 * A {@link RemoteAppenderClient} that reads serialized {@link ILoggingEvent} 
 * objects from an {@link InputStream}.
 *
 * @author Carl Harris
 */
class RemoteAppenderStreamClient implements RemoteAppenderClient {

    private final String id;
    private final Socket socket;
    private final InputStream inputStream;

    private LoggerContext lc;
    private Logger logger;

    /**
     * Constructs a new client.  
     * @param id a display name for the client
     * @param inputStream input stream from which events will be read
     */
    public RemoteAppenderStreamClient(String id, Socket socket) {
        this.id = id;
        this.socket = socket;
        this.inputStream = null;
    }

    /**
     * Constructs a new client.
     * <p>
     * This constructor is provided primarily to support unit tests for which
     * it is inconvenient to create a socket.
     *  
     * @param id a display name for the client
     * @param inputStream input stream from which events will be read
     */
    public RemoteAppenderStreamClient(String id, InputStream inputStream) {
        this.id = id;
        this.socket = null;
        this.inputStream = inputStream;
    }

    /**
     * {@inheritDoc}
     */
    public void setLoggerContext(LoggerContext lc) {
        this.lc = lc;
        this.logger = lc.getLogger(getClass().getPackage().getName());
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (socket == null)
            return;
        CloseUtil.closeQuietly(socket);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        logger.info(this + ": connected");
        HardenedObjectInputStream ois = null;
        try {
            ois = createObjectInputStream();
            while (true) {
                // read an event from the wire
                ILoggingEvent event = (ILoggingEvent) ois.readObject();
                // get a logger from the hierarchy. The name of the logger is taken to
                // be the name contained in the event.
                Logger remoteLogger = lc.getLogger(event.getLoggerName());
                // apply the logger-level filter
                if (remoteLogger.isEnabledFor(event.getLevel())) {
                    // finally log the event as if was generated locally
                    remoteLogger.callAppenders(event);
                }
            }
        } catch (EOFException ex) {
            // this is normal and expected
            assert true;
        } catch (IOException ex) {
            logger.info(this + ": " + ex);
        } catch (ClassNotFoundException ex) {
            logger.error(this + ": unknown event class");
        } catch (RuntimeException ex) {
            logger.error(this + ": " + ex);
        } finally {
            if (ois != null) {
                CloseUtil.closeQuietly(ois);
            }
            close();
            logger.info(this + ": connection closed");
        }
    }

    private HardenedObjectInputStream createObjectInputStream() throws IOException {
        if (inputStream != null) {
            return new HardenedLoggingEventInputStream(inputStream);
        }
        return new HardenedLoggingEventInputStream(socket.getInputStream());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "client " + id;
    }

}
