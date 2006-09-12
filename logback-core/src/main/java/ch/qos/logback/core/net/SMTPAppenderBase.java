/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.net;

import java.util.Date;
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
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.util.OptionHelper;

/**
 * An abstract class that provides basic support for
 * sending events to an email address.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public abstract class SMTPAppenderBase extends AppenderBase {
  
  
  protected Layout layout;
  protected Layout subjectLayout;

  private String to;
  private String from;
  private String subjectStr = null;
  private String smtpHost;

  protected Message msg;

  protected TriggeringPolicy evaluator;

  /**
   * return a layout for the subjet string as appropriate for the
   * module. If the subjectStr parameter is null, then a default
   * value for subjectStr should be used.
   * 
   * @param subjectStr
   * 
   * @return a layout as appropriate for the module
   */
  abstract protected Layout makeSubjectLayout(String subjectStr);
  
  /**
   * Start the appender
   */
  public void start() {
    Properties props = new Properties(System.getProperties());
    if (smtpHost != null) {
      props.put("mail.smtp.host", smtpHost);
    }

    Session session = Session.getInstance(props, null);
    // session.setDebug(true);
    msg = new MimeMessage(session);

    try {
      if (from != null) {
        msg.setFrom(getAddress(from));
      } else {
        msg.setFrom();
      }

      msg.setRecipients(Message.RecipientType.TO, parseAddress(to));
      
      subjectLayout = makeSubjectLayout(subjectStr);
      
      started = true;

    } catch (MessagingException e) {
      addError("Could not activate SMTPAppender options.", e);
    }
  }

  /**
   * Perform SMTPAppender specific appending actions, delegating some
   * of them to a subclass and checking if the event triggers an e-mail to be sent.
   */
  protected void append(Object eventObject) {

    if (!checkEntryConditions()) {
      return;
    }

    subAppend(eventObject);

    if (evaluator.isTriggeringEvent(null, eventObject)) {
      sendBuffer(eventObject);
    }
  }
  
  abstract protected void subAppend(Object eventObject);

  /**
   * This method determines if there is a sense in attempting to append.
   * 
   * <p>
   * It checks whether there is a set output target and also if there is a set
   * layout. If these checks fail, then the boolean value <code>false</code>
   * is returned.
   */
  public boolean checkEntryConditions() {
    if (this.msg == null) {
      addError("Message object not configured.");
      return false;
    }

    if (this.evaluator == null) {
      addError("No TriggeringPolicy is set for appender [" + name + "].");
      return false;
    }

    if (this.layout == null) {
      addError("No layout set for appender named [" + name + "].");
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

  InternetAddress[] parseAddress(String addressStr) {
    try {
      return InternetAddress.parse(addressStr, true);
    } catch (AddressException e) {
      addError("Could not parse address [" + addressStr + "].", e);
      return null;
    }
  }

  /**
   * Returns value of the <b>To</b> option.
   */
  public String getTo() {
    return to;
  }

  /**
   * Send the contents of the cyclic buffer as an e-mail message.
   */
  protected void sendBuffer(Object lastEventObject) {

    // Note: this code already owns the monitor for this
    // appender. This frees us from needing to synchronize on 'cb'.
    try {
      MimeBodyPart part = new MimeBodyPart();

      StringBuffer sbuf = new StringBuffer();
      
      String header = layout.getHeader();
      if (header != null) {
        sbuf.append(header);
      }
      fillBuffer(sbuf);
      String footer = layout.getFooter();
      if (footer != null) {
        sbuf.append(footer);
      }
      
      if (subjectLayout != null) {
        msg.setSubject(subjectLayout.doLayout(lastEventObject));
      }
      
      part.setContent(sbuf.toString(), layout.getContentType());

      Multipart mp = new MimeMultipart();
      mp.addBodyPart(part);
      msg.setContent(mp);

      msg.setSentDate(new Date());
      Transport.send(msg);
    } catch (Exception e) {
      addError("Error occured while sending e-mail notification.", e);
    }
  }
  
  abstract protected void fillBuffer(StringBuffer sbuf); 

  /**
   * Returns value of the <b>EvaluatorClass</b> option.
   */
  public String getEvaluatorClass() {
    return evaluator == null ? null : evaluator.getClass().getName();
  }

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
   * The <b>To</b> option takes a string value which should be a comma
   * separated list of e-mail address of the recipients.
   */
  public void setTo(String to) {
    this.to = to;
  }
  
  //for testing purpose only
  public Message getMessage() {
    return msg;
  }
  
  //for testing purpose only
  public void setMessage(Message msg) {
    this.msg = msg;
  }
  
  public void setEvaluator(TriggeringPolicy evaluator) {
    this.evaluator = evaluator;
  }

  /**
   * The <b>EvaluatorClass</b> option takes a string value representing the
   * name of the class implementing the {@link TriggeringEventEvaluator}
   * interface. A corresponding object will be instantiated and assigned as the
   * triggering event evaluator for the SMTPAppender.
   */
  public void setEvaluatorClass(String value) {
    try {
      evaluator = (TriggeringPolicy) OptionHelper.instantiateByClassName(value,
          TriggeringPolicy.class);
    } catch (Exception ex) {
      addError("Evaluator class instanciation failed");
    }
  }
  
  public Layout getLayout() {
    return layout;
  }

  public void setLayout(Layout layout) {
    this.layout = layout;
  }
}
