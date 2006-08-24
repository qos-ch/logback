/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.classic.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

/**
 * Sends {@link LoggingEvent} objects to a remote a log server, usually a
 * {@link SocketNode}.
 * 
 * <p>
 * The SocketAppender has the following properties:
 * 
 * <ul>
 * 
 * <p>
 * <li>If sent to a {@link SocketNode}, remote logging is non-intrusive as far
 * as the log event is concerned. In other words, the event will be logged with
 * the same time stamp, {@link org.apache.log4j.NDC}, location info as if it
 * were logged locally by the client.
 * 
 * <p>
 * <li>SocketAppenders do not use a layout. They ship a serialized
 * {@link LoggingEvent} object to the server side.
 * 
 * <p>
 * <li>Remote logging uses the TCP protocol. Consequently, if the server is
 * reachable, then log events will eventually arrive at the server.
 * 
 * <p>
 * <li>If the remote server is down, the logging requests are simply dropped.
 * However, if and when the server comes back up, then event transmission is
 * resumed transparently. This transparent reconneciton is performed by a
 * <em>connector</em> thread which periodically attempts to connect to the
 * server.
 * 
 * <p>
 * <li>Logging events are automatically <em>buffered</em> by the native TCP
 * implementation. This means that if the link to server is slow but still
 * faster than the rate of (log) event production by the client, the client will
 * not be affected by the slow network connection. However, if the network
 * connection is slower then the rate of event production, then the client can
 * only progress at the network rate. In particular, if the network link to the
 * the server is down, the client will be blocked.
 * 
 * <p>
 * On the other hand, if the network link is up, but the server is down, the
 * client will not be blocked when making log requests but the log events will
 * be lost due to server unavailability.
 * 
 * <p>
 * <li>Even if a <code>SocketAppender</code> is no longer attached to any
 * category, it will not be garbage collected in the presence of a connector
 * thread. A connector thread exists only if the connection to the server is
 * down. To avoid this garbage collection problem, you should {@link #close} the
 * the <code>SocketAppender</code> explicitly. See also next item.
 * 
 * <p>
 * Long lived applications which create/destroy many <code>SocketAppender</code>
 * instances should be aware of this garbage collection problem. Most other
 * applications can safely ignore it.
 * 
 * <p>
 * <li>If the JVM hosting the <code>SocketAppender</code> exits before the
 * <code>SocketAppender</code> is closed either explicitly or subsequent to
 * garbage collection, then there might be untransmitted data in the pipe which
 * might be lost. This is a common problem on Windows based systems.
 * 
 * <p>
 * To avoid lost data, it is usually sufficient to {@link #close} the
 * <code>SocketAppender</code> either explicitly or by calling the
 * {@link org.apache.log4j.LogManager#shutdown} method before exiting the
 * application.
 * 
 * 
 * </ul>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * @since 0.8.4
 */

public class SocketAppender extends AppenderBase {

	/**
	 * The default port number of remote logging server (4560).
	 */
	static final int DEFAULT_PORT = 4560;

	/**
	 * The default reconnection delay (30000 milliseconds or 30 seconds).
	 */
	static final int DEFAULT_RECONNECTION_DELAY = 30000;

	/**
	 * We remember host name as String in addition to the resolved InetAddress so
	 * that it can be returned via getOption().
	 */
	String remoteHost;

	InetAddress address;
	int port = DEFAULT_PORT;
	ObjectOutputStream oos;
	int reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

	private Connector connector;

	int counter = 0;

	// reset the ObjectOutputStream every 70 calls
	// private static final int RESET_FREQUENCY = 70;
	private static final int RESET_FREQUENCY = 1;

	public SocketAppender() {
	}

	/**
	 * Connects to remote server at <code>address</code> and <code>port</code>.
	 */
	public SocketAppender(InetAddress address, int port) {
		this.address = address;
		this.remoteHost = address.getHostName();
		this.port = port;
		//connect(address, port);
	}

	/**
	 * Connects to remote server at <code>host</code> and <code>port</code>.
	 */
	public SocketAppender(String host, int port) {
		this.port = port;
		this.address = getAddressByName(host);
		this.remoteHost = host;
		//connect(address, port);
	}

	//	/**
	//	 * Connect to the specified <b>RemoteHost</b> and <b>Port</b>.
	//	 */
	//	public void activateOptions() {
	//		connect(address, port);
	//	}

	/**
	 * Start this appender.
	 */
	public void start() {
		int errorCount = 0;
		if (port == 0) {
			errorCount++;
			addError("No port was configured for appender" + name);
		}

		if (address == null) {
			errorCount++;
			addError("No remote address was configured for appender" + name);
		}

		connect(address, port);

		if (errorCount == 0) {
			this.started = true;
		}
	}

	/**
	 * Strop this appender.
	 * 
	 * <p>
	 * This will mark the appender as closed and call then {@link #cleanUp}
	 * method.
	 */
	@Override
	public void stop() {
		if (!isStarted())
			return;

		this.started = false;
		cleanUp();
	}

	/**
	 * Drop the connection to the remote host and release the underlying connector
	 * thread if it has been created
	 */
	public void cleanUp() {
		if (oos != null) {
			try {
				oos.close();
			} catch (IOException e) {
				addError("Could not close oos.", e);
			}
			oos = null;
		}
		if (connector != null) {
			addInfo("Interrupting the connector.");
			connector.interrupted = true;
			connector = null; // allow gc
		}
	}

	void connect(InetAddress address, int port) {
		if (this.address == null)
			return;
		try {
			// First, close the previous connection if any.
			cleanUp();
			oos = new ObjectOutputStream(new Socket(address, port).getOutputStream());
		} catch (IOException e) {

			String msg = "Could not connect to remote log4j server at ["
					+ address.getHostName() + "].";
			if (reconnectionDelay > 0) {
				msg += " We will try again later.";
				fireConnector(); // fire the connector thread
			}
			addError(msg, e);
		}
	}

	@Override
	protected void append(Object event) {

		if (event == null)
			return;

		if (address == null) {
			addError("No remote host is set for SocketAppender named \"" + this.name
					+ "\".");
			return;
		}

		if (oos != null) {
			try {
				oos.writeObject(event);
				addInfo("=========Flushing.");
				oos.flush();
				if (++counter >= RESET_FREQUENCY) {
					counter = 0;
					// Failing to reset the object output stream every now and
					// then creates a serious memory leak.
					// System.err.println("Doing oos.reset()");
					oos.reset();
				}
			} catch (IOException e) {
				oos = null;
				addWarn("Detected problem with connection: " + e);
				if (reconnectionDelay > 0) {
					fireConnector();
				}
			}
		}
	}

	void fireConnector() {
		if (connector == null) {
			addInfo("Starting a new connector thread.");
			connector = new Connector();
			connector.setDaemon(true);
			connector.setPriority(Thread.MIN_PRIORITY);
			connector.start();
		}
	}

	static InetAddress getAddressByName(String host) {
		try {
			return InetAddress.getByName(host);
		} catch (Exception e) {
			// addError("Could not find address of [" + host + "].", e);
			return null;
		}
	}

	/**
	 * The SocketAppender does not use a layout. Hence, this method returns
	 * <code>false</code>.
	 */
	public boolean requiresLayout() {
		return false;
	}

	/**
	 * The <b>RemoteHost</b> option takes a string value which should be the host
	 * name of the server where a {@link SocketNode} is running.
	 */
	public void setRemoteHost(String host) {
		address = getAddressByName(host);
		remoteHost = host;
	}

	/**
	 * Returns value of the <b>RemoteHost</b> option.
	 */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * The <b>Port</b> option takes a positive integer representing the port
	 * where the server is waiting for connections.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns value of the <b>Port</b> option.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * The <b>ReconnectionDelay</b> option takes a positive integer representing
	 * the number of milliseconds to wait between each failed connection attempt
	 * to the server. The default value of this option is 30000 which corresponds
	 * to 30 seconds.
	 * 
	 * <p>
	 * Setting this option to zero turns off reconnection capability.
	 */
	public void setReconnectionDelay(int delay) {
		this.reconnectionDelay = delay;
	}

	/**
	 * Returns value of the <b>ReconnectionDelay</b> option.
	 */
	public int getReconnectionDelay() {
		return reconnectionDelay;
	}

	public Layout getLayout() {
		return null;
	}

	public void setLayout(Layout layout) {
	}

	/**
	 * The Connector will reconnect when the server becomes available again. It
	 * does this by attempting to open a new connection every
	 * <code>reconnectionDelay</code> milliseconds.
	 * 
	 * <p>
	 * It stops trying whenever a connection is established. It will restart to
	 * try reconnect to the server when previpously open connection is droppped.
	 * 
	 * @author Ceki G&uuml;lc&uuml;
	 * @since 0.8.4
	 */
	class Connector extends Thread {

		boolean interrupted = false;

		public void run() {
			Socket socket;
			while (!interrupted) {
				try {
					sleep(reconnectionDelay);
					addInfo("Attempting connection to " + address.getHostName());
					socket = new Socket(address, port);
					synchronized (this) {
						oos = new ObjectOutputStream(socket.getOutputStream());
						connector = null;
						addInfo("Connection established. Exiting connector thread.");
						break;
					}
				} catch (InterruptedException e) {
					addInfo("Connector interrupted. Leaving loop.");
					return;
				} catch (java.net.ConnectException e) {
					addInfo("Remote host " + address.getHostName()
							+ " refused connection.");
				} catch (IOException e) {
					addInfo("Could not connect to " + address.getHostName()
							+ ". Exception is " + e);
				}
			}
			// addInfo("Exiting Connector.run() method.");
		}

		/**
		 * public void finalize() { LogLog.debug("Connector finalize() has been
		 * called."); }
		 */
	}

}
