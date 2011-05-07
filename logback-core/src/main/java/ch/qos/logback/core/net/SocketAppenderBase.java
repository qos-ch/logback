/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.core.net;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * 
 * This is the base class for module specific SocketAppender implementations.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * @author J&ouml;rn Huxhorn
 */

public abstract class SocketAppenderBase<E> extends UnsynchronizedAppenderBase<E> {
  private static final int MAX_PORT = 65535;
  /**
   * The default port number of remote logging server (4560).
   */
  public static final int DEFAULT_PORT = 4560;

  /**
   * The default reconnection delay (30000 milliseconds or 30 seconds).
   */
  public static final int DEFAULT_RECONNECTION_DELAY = 30000;

  /**
   * The default connection timeout (10000 milliseconds or 10 seconds).
   */
  private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

  /**
   * We remember host name as String in addition to the resolved InetAddress so
   * that it can be returned via getOption().
   */
  protected String remoteHost;

  protected int port = DEFAULT_PORT;
  private final AtomicReference<ObjectOutputStream> atomicOutputStream=new AtomicReference<ObjectOutputStream>();

  private int reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

  private Thread connectorThread;

  private int counter = 0;
  private int connectionTimeout=DEFAULT_CONNECTION_TIMEOUT;

  /**
   * Start this appender.
   */
  public void start() {
    int errorCount = 0;
    if (port == 0) {
      errorCount++;
      addError("No port was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
    }

    if (port > MAX_PORT) {
      errorCount++;
      addError("Invalid port " + port + " was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_invalid_port");
    }

    if (remoteHost == null) {
      errorCount++;
      addError("No remote address was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
    }

    cleanUp();
    try {
      closeAndIgnore(atomicOutputStream.getAndSet(createOutputStream()));
    } catch (IOException e) {
      String msg = "Could not connect to remote logback server at ["  + remoteHost + "].";
      if (reconnectionDelay > 0) {
        msg += " We will try again later.";
        fireConnector(); // fire the connector thread
      }
      addInfo(msg, e);
    }

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
    performAdditionalCleanup(); // see comment on method
  }

  /**
   * Drop the connection to the remote host and release the underlying connector
   * thread if it has been created
   */
  private void cleanUp() {
    Thread t;
    synchronized(this) {
      t=connectorThread;
    }

    if (t != null) {
      addInfo("Interrupting the connector.");
      t.interrupt();
      try {
        t.join();
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    closeAndIgnore(atomicOutputStream.getAndSet(null));
  }


  private ObjectOutputStream createOutputStream() throws IOException {
    if (this.remoteHost == null)
      return null;
    if (this.port <= 0 || this.port > MAX_PORT)
      return null;

    InetAddress address = getAddressByName(remoteHost);
    if(address == null) {
      return null;
    }

    Socket socket = new Socket();
    SocketAddress socketAddress = new InetSocketAddress(address, port);

    socket.connect(socketAddress, connectionTimeout);

    try {
         return new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    } catch(IOException ex) {
      socket.close();
      throw ex;
    }
  }

  @Override
  protected void append(E event) {

    if (event == null)
      return;

    ObjectOutputStream oos = atomicOutputStream.get();
    if (oos != null) {
      postProcessEvent(event);
      Serializable serEvent = getPST().transform(event);
      try {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (oos) {
          oos.writeObject(serEvent);
          oos.flush();
          if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
            counter = 0;
            // Failing to reset the object output stream every now and
            // then creates a serious memory leak.
            // System.err.println("Doing oos.reset()");
            oos.reset();
          }
        }
      } catch (IOException e) {
        closeAndIgnore(oos);
        closeAndIgnore(atomicOutputStream.getAndSet(null));
        addWarn("Detected problem with connection: " + e);
        if (reconnectionDelay > 0 && started) {
          // only create connector if reconnectionDelay has been defined
          // and this appender has not already been stopped
          fireConnector();
        }
      }
    }
  }

  private void closeAndIgnore(ObjectOutputStream outputStream) {
    if(outputStream != null) {
      try {
        outputStream.reset();
      } catch (IOException ignore) {
      }
      try {
        outputStream.close();
      } catch (IOException ignore) {
      }
    }
  }

  protected abstract void postProcessEvent(E event);
  protected abstract PreSerializationTransformer<E> getPST();

  private synchronized void fireConnector() {
    if (connectorThread == null) {
      addInfo("Starting a new connector thread.");
      connectorThread = new Thread(new Connector(), name+" Connector");
      connectorThread.setDaemon(true);
      connectorThread.start();
    }
  }

  protected static InetAddress getAddressByName(String host) {
    try {
      return InetAddress.getByName(host);
    } catch (Exception e) {
      // addError("Could not find address of [" + host + "].", e);
      return null;
    }
  }

  /**
   * The <b>RemoteHost</b> property takes the name of of the host where a corresponding server is running.
   */
  public void setRemoteHost(String host) {
    remoteHost = host;
  }

  /**
   * Returns value of the <b>RemoteHost</b> property.
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * The <b>Port</b> property takes a positive integer representing the port
   * where the server is waiting for connections.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Returns value of the <b>Port</b> property.
   */
  public int getPort() {
    return port;
  }

  /**
   * The <b>reconnectionDelay</b> property takes a positive integer representing
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
   * Returns value of the <b>reconnectionDelay</b> property.
   */
  public int getReconnectionDelay() {
    return reconnectionDelay;
  }

  /**
   * Returns value of the <b>connectionTimeout</b> property.
   *
   * @return the connection timeout in milliseconds.
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * The <b>connectionTimeout</b> property takes a positive integer representing
   * the number of milliseconds until a connection attempt to a server timeouts.
   * The default value of this option is 10000 which corresponds to 10 seconds.
   *
   * <p>
   * A timeout of zero is interpreted as an infinite timeout. The connection
   * will then block until established or an error occurs.
   * @param connectionTimeout connection timeout in milliseconds
   */
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * This method performs additional cleanup to work around the issue described in LBCORE-205.
   *
   * The class java.io.ObjectStreamClass, which is used for serialization, has an internal class Caches
   * that keeps SoftReferences to classes previously (de)serialized. This causes a semi-leak since it prevents
   * unloading of those classes (and their classloader, including all other loaded classes) until memory is
   * running really low, forcing the SoftReferences to be collected by the garbage collection.
   *
   * This, in turn, causes a leak warning in Tomcat 7 leak-detection.
   * One could argue that this is a false positive since the webapp will, at some point, be completely collected.
   *
   * Regardless of this point, the fix below will, beside fixing the warning, relieve stress from the garbage
   * collector, since the amount of memory kept by a webapp classloader can be quite significant.
   * Freeing up those resources as soon as possible does make sense in any case.
   *
   * Calling the method below should be quite safe.
   * All relevant exceptions (including SecurityException) are handled properly.
   * Clearing the maps in Caches is a safe operation, too, since they are instances of ConcurrentMap and clear()
   * is performed atomic. Sudden cleanup of those maps would occur "naturally" in case of low memory, anyway.
   *
   * The only small "downside" will be a little bit of slowdown in case of further (de)serialization directly
   * afterwards since the caches will need to be repopulated.
   */
  private void performAdditionalCleanup() {
    Throwable t=null;
    String className = "java.io.ObjectStreamClass$Caches";
    try {
      Class clazz = Class.forName(className);
      clearStaticMap(clazz, "localDescs");
      clearStaticMap(clazz, "reflectors");
    } catch (ClassNotFoundException e) {
      t = e;
    } catch (SecurityException e) {
      t = e;
    }

    if(t != null) {
      addWarn("Failed to perform cleanup. Could not load class "+className+".", t);
    }
  }


  private void clearStaticMap(Class clazz, String fieldName) {
    Throwable t=null;
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(null);
      if(value instanceof Map) {
        Map map = (Map) value;
        map.clear();
        addInfo("Cleared map '"+fieldName+"' of class "+clazz.getName()+".");
      } else {
        addWarn("Failed to clear field '" + fieldName +"' of class " + clazz.getName()+". Field is not a map but " + value + ".");
      }
    } catch (NoSuchFieldException e) {
      t = e;
    } catch (IllegalAccessException e) {
      t = e;
    } catch (SecurityException e) {
      t = e;
    }
    if(t != null) {
      addWarn("Failed to clear field '" + fieldName +"' of class " + clazz.getName()+".");
    }
  }

  /**
   * The Connector will reconnect when the server becomes available again. It
   * does this by attempting to open a new connection every
   * <code>reconnectionDelay</code> milliseconds.
   * 
   * <p>
   * It stops trying whenever a connection is established. It will restart to
   * try reconnect to the server when previously open connection is dropped.
   * 
   * @author Ceki G&uuml;lc&uuml;
   * @since 0.8.4
   */
  class Connector implements Runnable {

    public void run() {
      for(;;) {
        try {
          Thread.sleep(reconnectionDelay);
          addInfo("Attempting connection to " + remoteHost);
          closeAndIgnore(atomicOutputStream.getAndSet(createOutputStream()));
          addInfo("Connection established. Exiting connector thread.");
          break;
        } catch (InterruptedException e) {
          addInfo("Connector interrupted. Leaving loop.");
          break;
        } catch (java.net.ConnectException e) {
          closeAndIgnore(atomicOutputStream.getAndSet(null));
          addInfo("Remote host " + remoteHost + " refused connection.");
        } catch (IOException e) {
          closeAndIgnore(atomicOutputStream.getAndSet(null));          
          addInfo("Could not connect to " + remoteHost + ". Exception is " + e);
        }
      }
      synchronized(SocketAppenderBase.this) {
        connectorThread = null;
      }
      // addInfo("Exiting Connector.run() method.");
    }

    /**
     * public void finalize() { LogLog.debug("Connector finalize() has been
     * called."); }
     */
  }
}
