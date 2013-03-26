package ch.qos.logback.core.net.ssl.mock;

import ch.qos.logback.core.net.ssl.SSLConfigurable;

public class MockSSLConfigurable implements SSLConfigurable {

  private static final String[] EMPTY = new String[0];
  
  private String[] defaultProtocols = EMPTY;
  private String[] supportedProtocols = EMPTY;
  private String[] enabledProtocols = EMPTY;
  private String[] defaultCipherSuites = EMPTY;
  private String[] supportedCipherSuites = EMPTY;
  private String[] enabledCipherSuites = EMPTY;
  private boolean needClientAuth;
  private boolean wantClientAuth;

  public String[] getDefaultProtocols() {
    return defaultProtocols;
  }

  public void setDefaultProtocols(String[] defaultProtocols) {
    this.defaultProtocols = defaultProtocols;
  }

  public String[] getSupportedProtocols() {
    return supportedProtocols;
  }

  public void setSupportedProtocols(String[] supportedProtocols) {
    this.supportedProtocols = supportedProtocols;
  }

  public String[] getEnabledProtocols() {
    return enabledProtocols;
  }

  public void setEnabledProtocols(String[] enabledProtocols) {
    this.enabledProtocols = enabledProtocols;
  }

  public String[] getDefaultCipherSuites() {
    return defaultCipherSuites;
  }

  public void setDefaultCipherSuites(String[] defaultCipherSuites) {
    this.defaultCipherSuites = defaultCipherSuites;
  }

  public String[] getSupportedCipherSuites() {
    return supportedCipherSuites;
  }

  public void setSupportedCipherSuites(String[] supportedCipherSuites) {
    this.supportedCipherSuites = supportedCipherSuites;
  }

  public String[] getEnabledCipherSuites() {
    return enabledCipherSuites;
  }

  public void setEnabledCipherSuites(String[] enabledCipherSuites) {
    this.enabledCipherSuites = enabledCipherSuites;
  }

  public boolean isNeedClientAuth() {
    return needClientAuth;
  }

  public void setNeedClientAuth(boolean needClientAuth) {
    this.needClientAuth = needClientAuth;
  }

  public boolean isWantClientAuth() {
    return wantClientAuth;
  }

  public void setWantClientAuth(boolean wantClientAuth) {
    this.wantClientAuth = wantClientAuth;
  }

}
