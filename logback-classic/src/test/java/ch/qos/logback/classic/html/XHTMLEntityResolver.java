package ch.qos.logback.classic.html;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class XHTMLEntityResolver implements EntityResolver {

  // key: public id, value: relative path to DTD file
  static Map<String, String> entityMap = new HashMap<String, String>();

  static {
    entityMap.put("-//W3C//DTD XHTML 1.0 Strict//EN",
        "/dtd/xhtml1-strict.dtd");
    entityMap.put("-//W3C//ENTITIES Latin 1 for XHTML//EN",
        "/dtd/xhtml-lat1.ent");
    entityMap.put("-//W3C//ENTITIES Symbols for XHTML//EN",
        "/dtd/xhtml-symbol.ent");
    entityMap.put("-//W3C//ENTITIES Special for XHTML//EN",
        "/dtd/xhtml-special.ent");
  }

  public InputSource resolveEntity(String publicId, String systemId) {
    //System.out.println(publicId);
    final String relativePath = (String)entityMap.get(publicId);

    if (relativePath != null) {
      Class clazz = getClass();
      InputStream in =
        clazz.getResourceAsStream(relativePath);
      if (in == null) {
        return null;
      } else {
        return new InputSource(in);
      }
    } else {
      return null;
    }
  }
}
