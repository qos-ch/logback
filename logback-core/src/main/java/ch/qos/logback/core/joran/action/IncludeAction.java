/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.joran.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class IncludeAction extends Action {

  private static final String INCLUDED_TAG = "included";
  private static final String FILE_ATTR = "file";
  private static final String URL_ATTR = "url";
  private static final String RESOURCE_ATTR = "resource";
  private static final String OPTIONAL_ATTR = "optional";

  private String attributeInUse;
  private boolean optional;

  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
          throws ActionException {

    SaxEventRecorder recorder = new SaxEventRecorder(context);

    this.attributeInUse = null;
    this.optional = OptionHelper.toBoolean(attributes.getValue(OPTIONAL_ATTR), false);

    if (!checkAttributes(attributes)) {
      return;
    }

    InputStream in = getInputStream(ec, attributes);

    try {
      if (in != null) {
        parseAndRecord(in, recorder);
        // remove the <included> tag from the beginning and </included> from the end
        trimHeadAndTail(recorder);

        // offset = 2, because we need to get past this element as well as the end element
        ec.getJoranInterpreter().getEventPlayer().addEventsDynamically(recorder.saxEventList, 2);
      }
    } catch (JoranException e) {
      addError("Error while parsing  " + attributeInUse, e);
    } finally {
      close(in);
    }

  }

  void close(InputStream in) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException e) {
      }
    }
  }

  private boolean checkAttributes(Attributes attributes) {
    String fileAttribute = attributes.getValue(FILE_ATTR);
    String urlAttribute = attributes.getValue(URL_ATTR);
    String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

    int count = 0;

    if (!OptionHelper.isEmpty(fileAttribute)) {
      count++;
    }
    if (!OptionHelper.isEmpty(urlAttribute)) {
      count++;
    }
    if (!OptionHelper.isEmpty(resourceAttribute)) {
      count++;
    }

    if (count == 0) {
      addError("One of \"path\", \"resource\" or \"url\" attributes must be set.");
      return false;
    } else if (count > 1) {
      addError("Only one of \"file\", \"url\" or \"resource\" attributes should be set.");
      return false;
    } else if (count == 1) {
      return true;
    }
    throw new IllegalStateException("Count value [" + count
            + "] is not expected");
  }

  private InputStream getInputStreamByFilePath(String pathToFile) {
    try {
      return new FileInputStream(pathToFile);
    } catch (IOException ioe) {
      String errMsg = "File [" + pathToFile + "] does not exist.";
      addError(errMsg, ioe);
      return null;
    }
  }

  URL attributeToURL(String urlAttribute) {
    try {
      return new URL(urlAttribute);
    } catch (MalformedURLException mue) {
      String errMsg = "URL [" + urlAttribute + "] is not well formed.";
      addError(errMsg, mue);
      return null;
    }
  }

  private InputStream getInputStreamByUrl(URL url) {
    return openURL(url);
  }

  InputStream openURL(URL url) {
    try {
      return url.openStream();
    } catch (IOException e) {
      if (!optional) {
        String errMsg = "Failed to open [" + url.toString() + "]";
        addError(errMsg, e);
      }
      return null;
    }
  }

  URL resourceAsURL(String resourceAttribute) {
    URL url = Loader.getResourceBySelfClassLoader(resourceAttribute);
    if (url == null) {
      if (!optional) {
        String errMsg = "Could not find resource corresponding to ["
                + resourceAttribute + "]";
        addError(errMsg);
      }
      return null;
    } else
      return url;
  }

  URL filePathAsURL(String path) {
    URI uri = new File(path).toURI();
    try {
      return uri.toURL();
    } catch (MalformedURLException e) {
      // impossible to get here
      e.printStackTrace();
      return null;
    }
  }

  private InputStream getInputStreamByResource(URL url) {
    return openURL(url);
  }

  URL getInputURL(InterpretationContext ec, Attributes attributes) {
    String fileAttribute = attributes.getValue(FILE_ATTR);
    String urlAttribute = attributes.getValue(URL_ATTR);
    String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

    if (!OptionHelper.isEmpty(fileAttribute)) {
      this.attributeInUse = ec.subst(fileAttribute);
      return filePathAsURL(attributeInUse);
    }

    if (!OptionHelper.isEmpty(urlAttribute)) {
      this.attributeInUse = ec.subst(urlAttribute);
      return attributeToURL(attributeInUse);
    }

    if (!OptionHelper.isEmpty(resourceAttribute)) {
      this.attributeInUse = ec.subst(resourceAttribute);
      return resourceAsURL(attributeInUse);
    }
    // given previous checkAttributes() check we cannot reach this line
    throw new IllegalStateException("A URL stream should have been returned");

  }

  InputStream getInputStream(InterpretationContext ec, Attributes attributes) {
    URL inputURL = getInputURL(ec, attributes);
    if (inputURL == null)
      return null;

    ConfigurationWatchListUtil.addToWatchList(context, inputURL);
    return openURL(inputURL);
  }

  private void trimHeadAndTail(SaxEventRecorder recorder) {
    // Let's remove the two <included> events before
    // adding the events to the player.

    List<SaxEvent> saxEventList = recorder.saxEventList;

    if (saxEventList.size() == 0) {
      return;
    }

    SaxEvent first = saxEventList.get(0);
    if (first != null && first.qName.equalsIgnoreCase(INCLUDED_TAG)) {
      saxEventList.remove(0);
    }

    SaxEvent last = saxEventList.get(recorder.saxEventList.size() - 1);
    if (last != null && last.qName.equalsIgnoreCase(INCLUDED_TAG)) {
      saxEventList.remove(recorder.saxEventList.size() - 1);
    }
  }

  private void parseAndRecord(InputStream inputSource, SaxEventRecorder recorder)
          throws JoranException {
    recorder.setContext(context);
    recorder.recordEvents(inputSource);
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
    // do nothing
  }

}
