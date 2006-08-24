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

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * A {@link SocketNode} based server that uses a different hierarchy for each
 * client.
 * 
 * <pre>
 *    &lt;b&gt;Usage:&lt;/b&gt; java ch.qos.logback.classic.net.SocketServer port configFile configDir
 *   
 *    where &lt;b&gt;port&lt;/b&gt; is a part number where the server listens,
 *    &lt;b&gt;configFile&lt;/b&gt; is an xml configuration file fed to the {@link JoranConfigurator} and
 *    &lt;b&gt;configDir&lt;/b&gt; is a path to a directory containing configuration files, possibly one for each client host.
 * </pre>
 * 
 * <p>
 * The <code>configFile</code> is used to configure the log4j default
 * hierarchy that the <code>SocketServer</code> will use to report on its
 * actions.
 * 
 * <p>
 * When a new connection is opened from a previously unknown host, say
 * <code>foo.bar.net</code>, then the <code>SocketServer</code> will search
 * for a configuration file called <code>foo.bar.net.lcf</code> under the
 * directory <code>configDir</code> that was passed as the third argument. If
 * the file can be found, then a new hierarchy is instantiated and configured
 * using the configuration file <code>foo.bar.net.lcf</code>. If and when the
 * host <code>foo.bar.net</code> opens another connection to the server, then
 * the previously configured hierarchy is used.
 * 
 * <p>
 * In case there is no file called <code>foo.bar.net.lcf</code> under the
 * directory <code>configDir</code>, then the <em>generic</em> hierarchy is
 * used. The generic hierarchy is configured using a configuration file called
 * <code>generic.lcf</code> under the <code>configDir</code> directory. If
 * no such file exists, then the generic hierarchy will be identical to the
 * log4j default hierarchy.
 * 
 * <p>
 * Having different client hosts log using different hierarchies ensures the
 * total independence of the clients with respect to their logging settings.
 * 
 * <p>
 * Currently, the hierarchy that will be used for a given request depends on the
 * IP address of the client host. For example, two separate applicatons running
 * on the same host and logging to the same server will share the same
 * hierarchy. This is perfectly safe except that it might not provide the right
 * amount of independence between applications. The <code>SocketServer</code>
 * is intended as an example to be enhanced in order to implement more elaborate
 * policies.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @since 1.0
 */

public class SocketServer {

	static String GENERIC = "generic";
	static String CONFIG_FILE_EXT = ".lcf";

	static Logger logger = LoggerFactory.getLogger(SocketServer.class);
	static SocketServer server;
	static int port;

	// key=inetAddress, value=hierarchy
	Hashtable<InetAddress, LoggerContext> hierarchyMap;
	LoggerContext genericHierarchy;
	File dir;

	public static void main(String argv[]) {
		if (argv.length == 3) {
			init(argv[0], argv[1], argv[2]);
		} else {
			usage("Wrong number of arguments.");
		}

		try {
			logger.info("Listening on port " + port);
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				logger.info("Waiting to accept a new client.");
				Socket socket = serverSocket.accept();
				InetAddress inetAddress = socket.getInetAddress();
				logger.info("Connected to client at " + inetAddress);

				LoggerContext h = (LoggerContext) server.hierarchyMap.get(inetAddress);
				if (h == null) {
					h = server.configureHierarchy(inetAddress);
				}

				logger.info("Starting new socket node.");
				new Thread(new SocketNode(socket, h)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void usage(String msg) {
		System.err.println(msg);
		System.err.println("Usage: java " + SocketServer.class.getName()
				+ " port configFile directory");
		System.exit(1);
	}

	static void init(String portStr, String configFile, String dirStr) {
		try {
			port = Integer.parseInt(portStr);
		} catch (java.lang.NumberFormatException e) {
			e.printStackTrace();
			usage("Could not interpret port number [" + portStr + "].");
		}

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		configurator.doConfigure(configFile);

		File dir = new File(dirStr);
		if (!dir.isDirectory()) {
			usage("[" + dirStr + "] is not a directory.");
		}
		server = new SocketServer(dir);
	}

	public SocketServer(File directory) {
		this.dir = directory;
		hierarchyMap = new Hashtable<InetAddress, LoggerContext>(11);
	}

	// This method assumes that there is no hiearchy for inetAddress
	// yet. It will configure one and return it.
	LoggerContext configureHierarchy(InetAddress inetAddress) {
		logger.info("Locating configuration file for " + inetAddress);
		// We assume that the toSting method of InetAddress returns is in
		// the format hostname/d1.d2.d3.d4 e.g. torino/192.168.1.1
		String s = inetAddress.toString();
		int i = s.indexOf("/");
		if (i == -1) {
			logger.warn("Could not parse the inetAddress [" + inetAddress
					+ "]. Using default hierarchy.");
			return genericHierarchy();
		} else {
			String key = s.substring(0, i);

			File configFile = new File(dir, key + CONFIG_FILE_EXT);
			if (configFile.exists()) {
				LoggerContext lc = new LoggerContext();
				hierarchyMap.put(inetAddress, lc);

				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(lc);
				configurator.doConfigure(configFile);

				return lc;
			} else {
				logger.warn("Could not find config file [" + configFile + "].");
				return genericHierarchy();
			}
		}
	}

	LoggerContext genericHierarchy() {
		if (genericHierarchy == null) {
			File f = new File(dir, GENERIC + CONFIG_FILE_EXT);
			if (f.exists()) {
				genericHierarchy = new LoggerContext();
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(genericHierarchy);
				configurator.doConfigure(f.getAbsolutePath());

			} else {
				logger.warn("Could not find config file [" + f
						+ "]. Will use the default hierarchy.");
				genericHierarchy = new LoggerContext();
			}
		}
		return genericHierarchy;
	}
}
