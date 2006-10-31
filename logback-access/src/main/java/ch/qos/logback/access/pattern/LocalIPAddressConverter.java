package ch.qos.logback.access.pattern;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.qos.logback.access.spi.AccessEvent;

public class LocalIPAddressConverter extends AccessConverter {

  String localIPAddressStr;

  public LocalIPAddressConverter() {
    try {
      localIPAddressStr = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException uhe) {
      localIPAddressStr = "127.0.0.1";
    }
  }

  protected String convert(AccessEvent accessEvent) {
    return localIPAddressStr;
  }

}
