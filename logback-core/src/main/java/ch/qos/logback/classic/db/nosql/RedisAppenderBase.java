package ch.qos.logback.classic.db.nosql;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <p>Redis appender common functionality.</p>
 *
 *
 * @author Juan Uys
 */
public abstract class RedisAppenderBase<E> extends UnsynchronizedAppenderBase<E> {

  // user-specified
  private int port;
  private String password;
  private String hostName;
  private int timeout;
  private String uniqueKeyPrefix;

  private Jedis client;

  // forms the common part of all keys
  private String keyBase;

  protected void init() {
    // initialise the Redis client
    if (getPort() != 0 && getTimeout() != 0) {
      client = new Jedis(getHostName(), getPort(), getTimeout());
    } else if (getPort() != 0) {
      client = new Jedis(getHostName(), getPort());
    } else {
      client = new Jedis(getHostName());
    }
    try {
      client.connect();
      if (getPassword() != null) {
        client.auth(getPassword());
      }
    } catch (UnknownHostException e) {
      addError("Could not get Redis Connection", e);
    } catch (IOException e) {
      addError("Could not get Redis Connection", e);
    } catch (Throwable e) {
      addError("Unexpected exception on creating Redis Client", e);
    }

    // set up the common key base
    StringBuffer keyB = new StringBuffer();
    if (uniqueKeyPrefix != null && !uniqueKeyPrefix.trim().equals("")) {
        keyB = keyB.append(uniqueKeyPrefix).append(':');
      } else {
        keyB = keyB.append("logback:");
      }

    try {
      keyB = keyB.append(InetAddress.getLocalHost().getHostName()).append(':');
    } catch (UnknownHostException e) {
      addError("Failed to get local hostname", e);
    }
    keyBase = keyB.toString();
  }

  @Override
  public void start() {
    init();
    super.start();    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public void stop() {
    try {
      client.disconnect();
    } catch (IOException e) {
      addError("Could not close Redis Channel", e);
    } catch (Throwable e) {
      addError("Unexpected exception on closing Redis Client", e);
    }
    super.stop();
  }

  @Override
  protected void append(E eventObject) {
    subAppend(eventObject, getEventId());
  }

  abstract public void subAppend(E event, Integer eventId);

  private int getPort() {
    return port;
  }

  private String getPassword() {
    return password;
  }

  private String getHostName() {
    return hostName;
  }

  public String getKeyBase() {
    return keyBase;
  }

  private int getTimeout() {
    return timeout;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public void setUniqueKeyPrefix(String uniqueKeyPrefix) {
    this.uniqueKeyPrefix = uniqueKeyPrefix;
  }

  protected void setClient(Jedis client) {
    this.client = client;
  }

  public void set(String key, Object value) {
    client.set(getKeyBase() + key, String.valueOf(value));
  }

  public Integer getEventId() {
    return client.incr(keyBase+"id");
  }
}