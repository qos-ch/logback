/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.net;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.util.ContentTypeUtil;
import ch.qos.logback.core.util.OptionHelper;

// Contributors:
// Andrey Rybin charset encoding support http://jira.qos.ch/browse/LBCORE-69

/**
 * An abstract class that provides support for sending events to an email
 * address.
 * 
 * <p>See http://logback.qos.ch/manual/appenders.html#SMTPAppender for further
 * documentation.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public abstract class SMTPAppenderBase<E> extends AppenderBase<E> {

  protected Layout<E> subjectLayout;

  private List<String> to = new ArrayList<String>();
  private String from;
  private String subjectStr = null;
  private String smtpHost;
  private int smtpPort = 25;
  private boolean starttls = false;
  private boolean ssl = false;

  String username;
  String password;

  private String charsetEncoding = "UTF-8";

  protected MimeMessage mimeMsg;

  protected EventEvaluator<E> eventEvaluator;

  /**
   * return a layout for the subjet string as appropriate for the module. If the
   * subjectStr parameter is null, then a default value for subjectStr should be
   * used.
   * 
   * @param subjectStr
   * 
   * @return a layout as appropriate for the module
   */
  abstract protected Layout<E> makeSubjectLayout(String subjectStr);

  /**
   * Start the appender
   */
  public void start() {
    Properties props = new Properties(OptionHelper.getSystemProperties());
    if (smtpHost != null) {
      props.put("mail.smtp.host", smtpHost);
    }
    props.put("mail.smtp.port", Integer.toString(smtpPort));

    LoginAuthenticator loginAuthenticator = null;

    if (username != null) {
      loginAuthenticator = new LoginAuthenticator(username, password);
      props.put("mail.smtp.auth", "true");
    }

    if (isSTARTTLS() && isSSL()) {
      addError("Both SSL and StartTLS cannot be enabled simultaneously");
    } else {
      if (isSTARTTLS()) {
        props.setProperty("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
      }
      if (isSSL()) {
        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        props.put("mail.smtp.socketFactory.port", Integer.toString(smtpPort));
        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.put("mail.smtp.socketFactory.fallback", "true");
      }
    }

    // props.put("mail.debug", "true");

    Session session = Session.getInstance(props, loginAuthenticator);
    mimeMsg = new MimeMessage(session);

    try {
      if (from != null) {
        mimeMsg.setFrom(getAddress(from));
      } else {
        mimeMsg.setFrom();
      }

      mimeMsg.setRecipients(Message.RecipientType.TO, parseAddress(to));

      subjectLayout = makeSubjectLayout(subjectStr);

      started = true;

    } catch (MessagingException e) {
      addError("Could not activate SMTPAppender options.", e);
    }
  }

  /**
   * Perform SMTPAppender specific appending actions, delegating some of them to
   * a subclass and checking if the event triggers an e-mail to be sent.
   */
  protected void append(E eventObject) {

    if (!checkEntryConditions()) {
      return;
    }

    subAppend(eventObject);

    try {
      if (eventEvaluator.evaluate(eventObject)) {
        sendBuffer(eventObject);
      }
    } catch (EvaluationException ex) {
      addError("SMTPAppender's EventEvaluator threw an Exception" + ex);
    }
  }

  abstract protected void subAppend(E eventObject);

  /**
   * This method determines if there is a sense in attempting to append.
   * 
   * <p> It checks whether there is a set output target and also if there is a
   * set layout. If these checks fail, then the boolean value <code>false</code>
   * is returned.
   */
  public boolean checkEntryConditions() {
    if (!this.started) {
      addError("Attempting to append to a non-started appender: "
          + this.getName());
      return false;
    }

    if (this.mimeMsg == null) {
      addError("Message object not configured.");
      return false;
    }

    if (this.eventEvaluator == null) {
      addError("No EventEvaluator is set for appender [" + name + "].");
      return false;
    }

    if (this.layout == null) {
      addError("No layout set for appender named ["
          + name
          + "]. For more information, please visit http://logback.qos.ch/codes.html#smtp_no_layout");
      return false;
    }
    return true;
  }

  synchronized public void stop() {
    this.started = false;
  }

  InternetAddress getAddress(String addressStr) {
    try {
      return new InternetAddress(addressStr);
    } catch (AddressException e) {
      addError("Could not parse address [" + addressStr + "].", e);
      return null;
    }
  }

  InternetAddress[] parseAddress(List<String> addressList) {

    InternetAddress[] iaArray = new InternetAddress[addressList.size()];

    for (int i = 0; i < addressList.size(); i++) {
      try {
        InternetAddress[] tmp = InternetAddress.parse(addressList.get(i), true);
        // one <To> element should contain one email address
        iaArray[i] = tmp[0];
      } catch (AddressException e) {
        addError("Could not parse address [" + addressList.get(i) + "].", e);
        return null;
      }
    }

    return iaArray;
  }

  /**
   * Returns value of the <b>To</b> option.
   */
  public List<String> getTo() {
    return to;
  }

  /**
   * Send the contents of the cyclic buffer as an e-mail message.
   */
  protected void sendBuffer(E lastEventObject) {

    // Note: this code already owns the monitor for this
    // appender. This frees us from needing to synchronize on 'cb'.
    try {
      MimeBodyPart part = new MimeBodyPart();

      StringBuffer sbuf = new StringBuffer();

      String header = layout.getFileHeader();
      if (header != null) {
        sbuf.append(header);
      }
      String presentationHeader = layout.getPresentationHeader();
      if (presentationHeader != null) {
        sbuf.append(presentationHeader);
      }
      fillBuffer(sbuf);
      String presentationFooter = layout.getPresentationFooter();
      if (presentationFooter != null) {
        sbuf.append(presentationFooter);
      }
      String footer = layout.getFileFooter();
      if (footer != null) {
        sbuf.append(footer);
      }

      if (subjectLayout != null) {
        mimeMsg.setSubject(subjectLayout.doLayout(lastEventObject),
            charsetEncoding);
      }

      String contentType = layout.getContentType();

      if (ContentTypeUtil.isTextual(contentType)) {
        part.setText(sbuf.toString(), charsetEncoding, ContentTypeUtil
            .getSubType(contentType));
      } else {
        part.setContent(sbuf.toString(), layout.getContentType());
      }

      Multipart mp = new MimeMultipart();
      mp.addBodyPart(part);
      mimeMsg.setContent(mp);

      mimeMsg.setSentDate(new Date());
      Transport.send(mimeMsg);
    } catch (Exception e) {
      addError("Error occured while sending e-mail notification.", e);
    }
  }

  abstract protected void fillBuffer(StringBuffer sbuf);

  /**
   * Returns value of the <b>From</b> option.
   */
  public String getFrom() {
    return from;
  }

  /**
   * Returns value of the <b>Subject</b> option.
   */
  public String getSubject() {
    return subjectStr;
  }

  /**
   * The <b>From</b> option takes a string value which should be a e-mail
   * address of the sender.
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * The <b>Subject</b> option takes a string value which should be a the
   * subject of the e-mail message.
   */
  public void setSubject(String subject) {
    this.subjectStr = subject;
  }

  /**
   * The <b>SMTPHost</b> option takes a string value which should be a the host
   * name of the SMTP server that will send the e-mail message.
   */
  public void setSMTPHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }

  /**
   * Returns value of the <b>SMTPHost</b> option.
   */
  public String getSMTPHost() {
    return smtpHost;
  }

  /**
   * The port where the SMTP server is running. Default value is 25.
   * 
   * @param port
   */
  public void setSMTPPort(int port) {
    this.smtpPort = port;
  }

  /**
   * @see #setSMTPPort(int)
   * @return
   */
  public int getSMTPPort() {
    return smtpPort;
  }

  /**
   * The <b>To</b> option takes a string value which should be an e-mail
   * address of one of the recipients.
   */
  public void addTo(String to) {
    this.to.add(to);
  }

  // for testing purpose only
  public Message getMessage() {
    return mimeMsg;
  }

  // for testing purpose only
  public void setMessage(MimeMessage msg) {
    this.mimeMsg = msg;
  }

  public boolean isSTARTTLS() {
    return starttls;
  }

  public void setSTARTTLS(boolean startTLS) {
    this.starttls = startTLS;
  }

  public boolean isSSL() {
    return ssl;
  }

  public void setSSL(boolean ssl) {
    this.ssl = ssl;
  }

  /**
   * The <b>EventEvaluator</b> option takes a string value representing the
   * name of the class implementing the {@link EventEvaluators} interface. A
   * corresponding object will be instantiated and assigned as the event
   * evaluator for the SMTPAppender.
   */
  public void setEvaluator(EventEvaluator<E> eventEvaluator) {
    this.eventEvaluator = eventEvaluator;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @see #setCharsetEncoding(String)
   * @return the charset encoding value
   */
  String getCharsetEncoding() {
    return charsetEncoding;
  }

  /**
   * Set the character set encoding of the outgoing email messages. The default
   * encoding is "UTF-8" which usually works well for most purposes.
   * 
   * @param charsetEncoding
   */
  void setCharsetEncoding(String charsetEncoding) {
    this.charsetEncoding = charsetEncoding;
  }

}
