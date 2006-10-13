package ch.qos.logback.core.joran;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.SaxEvent;
import ch.qos.logback.core.joran.spi.SaxEventRecorder;
import ch.qos.logback.core.spi.ContextAwareBase;

public class GenericConfigurator extends ContextAwareBase {

  final public void doConfigure(URL url) throws JoranException {
    try {
      InputStream in = url.openStream();
      doConfigure(in);
      in.close();
    } catch (IOException ioe) {
      String errMsg = "Could not open URL [" + url + "].";
      addError(errMsg, ioe);
      throw new JoranException(errMsg, ioe);
    }
  }

  final public void doConfigure(String filename) throws JoranException {
    doConfigure(new File(filename));
  }

  final public void doConfigure(File file) throws JoranException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      doConfigure(fis);
    } catch (IOException ioe) {
      String errMsg = "Could not open [" + file.getName() + "].";
      addError(errMsg, ioe);
      throw new JoranException(errMsg, ioe);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (java.io.IOException ioe) {
          String errMsg = "Could not close [" + file.getName() + "].";
          addError(errMsg, ioe);
          throw new JoranException(errMsg,ioe);
        }
      }
    }
  }

  final public void doConfigure(InputStream inputStream) throws JoranException {
    doConfigure(new InputSource(inputStream));
  }

  List<SaxEvent> recordEvents(InputSource inputSource) throws JoranException {
    SAXParser saxParser = null;
    SaxEventRecorder saxEventRecorder = new SaxEventRecorder();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      spf.setNamespaceAware(true);
      saxParser = spf.newSAXParser();
    } catch (Exception pce) {
      String errMsg = "Parser configuration error occured";
      addError(errMsg, pce);
      throw new JoranException(errMsg, pce);
    }

    try {
      saxParser.parse(inputSource, saxEventRecorder);
      return saxEventRecorder.saxEventList;

    } catch (IOException ie) {
      String errMsg = "I/O error occurred while parsing xml file";
      addError(errMsg, ie);
      throw new JoranException(errMsg, ie);
    } catch (Exception ex) {
      String errMsg = "Problem parsing XML document. See previously reported errors. Abandoning all further processing.";
      addError(errMsg, ex);
      throw new JoranException(errMsg, ex);
    }

  }

  final public void doConfigure(final InputSource inputSource)
      throws JoranException {
    
    List<SaxEvent> saxEventList;
    saxEventList = recordEvents(inputSource);
    
  }

}
