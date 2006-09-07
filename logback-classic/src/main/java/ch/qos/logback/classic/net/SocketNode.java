/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

// Contributors: Moses Hohman <mmhohman@rainbow.uchicago.edu>

/**
 * Read {@link LoggingEvent} objects sent from a remote client using Sockets
 * (TCP). These logging events are logged according to local policy, as if they
 * were generated locally.
 * 
 * <p>
 * For example, the socket node might decide to log events to a local file and
 * also resent them to a second socket node.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * @since 0.8.4
 */
public class SocketNode implements Runnable {

	Socket socket;
	LoggerContext context;
	ObjectInputStream ois;

	static Logger logger = (Logger) LoggerFactory.getLogger(SocketNode.class);

	public SocketNode(Socket socket, LoggerContext context) {
		this.socket = socket;
		this.context = context;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(socket
					.getInputStream()));
		} catch (Exception e) {
			logger.error("Could not open ObjectInputStream to " + socket, e);
		}
	}

	// public
	// void finalize() {
	// System.err.println("-------------------------Finalize called");
	// System.err.flush();
	// }

	public void run() {
		LoggingEvent event;
		Logger remoteLogger;

		try {
			while (true) {
				// read an event from the wire
				event = (LoggingEvent) ois.readObject();
				// get a logger from the hierarchy. The name of the logger is taken to
				// be the name contained in the event.
				remoteLogger = context.getLogger(event.getLoggerRemoteView().getName());
				// apply the logger-level filter
				if (remoteLogger.isEnabledFor(event.getLevel())) {
					// finally log the event as if was generated locally
					remoteLogger.callAppenders(event);
				}
			}
		} catch (java.io.EOFException e) {
			logger.info("Caught java.io.EOFException closing connection.");
		} catch (java.net.SocketException e) {
			logger.info("Caught java.net.SocketException closing connection.");
		} catch (IOException e) {
			logger.info("Caught java.io.IOException: " + e);
			logger.info("Closing connection.");
		} catch (Exception e) {
			logger.error("Unexpected exception. Closing connection.", e);
		}

		try {
			ois.close();
		} catch (Exception e) {
			logger.info("Could not close connection.", e);
		}
	}
}
