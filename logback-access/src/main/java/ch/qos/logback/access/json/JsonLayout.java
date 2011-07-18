package ch.qos.logback.access.json;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.json.JsonLayoutBase;

/**
 * A brain-dead implementation with no external dependencies
 * of a layout producing a simple JSON like this:
 * <code>
 * {
 *  "method": "testMethod",
 *  "protocol": "testProtocol",
 *  "remote-addr": "testRemoteAddress",
 *  "remote-host": "testHost",
 *  "remote-user": "testUser",
 *  "request-uri": "http://localhost:8080/test/index.html",
 *  "server-name": "testServerName",
 *  "status-code": "200", 
 *  "timestamp": 1311018444363 
 * }
 * </code>
 * @author Pierre Queinnec
 */
public class JsonLayout extends JsonLayoutBase<IAccessEvent> {

  protected boolean includeContentLength;
  protected boolean includeLocalPort;
  protected boolean includeMethod;
  protected boolean includeProtocol;
  protected boolean includeRemoteAddr;
  protected boolean includeRemoteHost;
  protected boolean includeRemoteUser;
  protected boolean includeRequestURI;
  protected boolean includeRequestURL;
  protected boolean includeServerName;
  protected boolean includeStatusCode;

  public JsonLayout() {
    super();

    // defaults
    this.includeMethod = true;
    this.includeProtocol = true;
    this.includeRemoteAddr = true;
    this.includeRemoteHost = true;
    this.includeRemoteUser = true;
    this.includeRequestURI = true;
    this.includeServerName = true;
    this.includeStatusCode = true;
  }

  public String doLayout(IAccessEvent event) {
    boolean hasAlreadyOneElement = false;
    StringBuilder builder = new StringBuilder();
    builder.append('{');

    if (this.includeContentLength) {
      hasAlreadyOneElement = true;

      builder.append("\"content-length\":");
      builder.append(event.getContentLength());
    }

    if (this.includeLocalPort) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"local-port\":");
      builder.append(event.getLocalPort());
    }

    if (this.includeMethod) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"method\":\"");
      builder.append(event.getMethod());
      builder.append('\"');
    }

    if (this.includeProtocol) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"protocol\":\"");
      builder.append(event.getProtocol());
      builder.append('\"');
    }

    if (this.includeRemoteAddr) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"remote-addr\":\"");
      builder.append(event.getRemoteAddr());
      builder.append('\"');
    }

    if (this.includeRemoteHost) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"remote-host\":\"");
      builder.append(event.getRemoteHost());
      builder.append('\"');
    }

    if (this.includeRemoteUser) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"remote-user\":\"");
      builder.append(event.getRemoteUser());
      builder.append('\"');
    }

    if (this.includeRequestURI) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"request-uri\":\"");
      builder.append(event.getRequestURI());
      builder.append('\"');
    }

    if (this.includeRequestURL) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"request-url\":\"");
      builder.append(event.getRequestURL());
      builder.append('\"');
    }

    if (this.includeServerName) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"server-name\":\"");
      builder.append(event.getServerName());
      builder.append('\"');
    }

    if (this.includeStatusCode) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"status-code\":");
      builder.append(event.getStatusCode());
    }

    if (this.includeTimestamp) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"timestamp\":");
      builder.append(event.getTimeStamp());
    }

    builder.append('}');

    return builder.toString();
  }

  public void setIncludeContentLength(boolean includeContentLength) {
    this.includeContentLength = includeContentLength;
  }

  public void setIncludeLocalPort(boolean includeLocalPort) {
    this.includeLocalPort = includeLocalPort;
  }

  public void setIncludeMethod(boolean includeMethod) {
    this.includeMethod = includeMethod;
  }

  public void setIncludeProtocol(boolean includeProtocol) {
    this.includeProtocol = includeProtocol;
  }

  public void setIncludeRemoteAddr(boolean includeRemoteAddr) {
    this.includeRemoteAddr = includeRemoteAddr;
  }

  public void setIncludeRemoteHost(boolean includeRemoteHost) {
    this.includeRemoteHost = includeRemoteHost;
  }

  public void setIncludeRemoteUser(boolean includeRemoteUser) {
    this.includeRemoteUser = includeRemoteUser;
  }

  public void setIncludeRequestURI(boolean includeRequestURI) {
    this.includeRequestURI = includeRequestURI;
  }

  public void setIncludeRequestURL(boolean includeRequestURL) {
    this.includeRequestURL = includeRequestURL;
  }

  public void setIncludeServerName(boolean includeServerName) {
    this.includeServerName = includeServerName;
  }

  public void setIncludeStatusCode(boolean includeStatusCode) {
    this.includeStatusCode = includeStatusCode;
  }

}
