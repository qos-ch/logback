/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;

/**
 * A simple application that consumes logging events sent by a {@link
 * JMSQueueAppender}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class JMSQueueSink implements javax.jms.MessageListener {

    private Logger logger = (Logger) LoggerFactory.getLogger(JMSTopicSink.class);

    static public void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage("Wrong number of arguments.");
        }

        String qcfBindingName = args[0];
        String queueBindingName = args[1];
        String username = null;
        String password = null;
        if (args.length == 4) {
            username = args[2];
            password = args[3];
        }

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        new ContextInitializer(loggerContext).autoConfig();

        new JMSQueueSink(qcfBindingName, queueBindingName, username, password);

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        // Loop until the word "exit" is typed
        System.out.println("Type \"exit\" to quit JMSQueueSink.");
        while (true) {
            String s = stdin.readLine();
            if (s.equalsIgnoreCase("exit")) {
                System.out.println("Exiting. Kill the application if it does not exit " + "due to daemon threads.");
                return;
            }
        }
    }

    public JMSQueueSink(String qcfBindingName, String queueBindingName, String username, String password) {

        try {
            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            Context ctx = new InitialContext(env);
            QueueConnectionFactory queueConnectionFactory;
            queueConnectionFactory = (QueueConnectionFactory) lookup(ctx, qcfBindingName);
            System.out.println("Queue Cnx Factory found");
            Queue queue = (Queue) ctx.lookup(queueBindingName);
            System.out.println("Queue found: " + queue.getQueueName());

            QueueConnection queueConnection = queueConnectionFactory.createQueueConnection(username, password);
            System.out.println("Queue Connection created");

            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer queueConsumer = queueSession.createConsumer(queue);

            queueConsumer.setMessageListener(this);

            queueConnection.start();
            System.out.println("Queue Connection started");

        } catch (Exception e) {
            logger.error("Could not read JMS message.", e);
        }
    }

    public void onMessage(javax.jms.Message message) {
        ILoggingEvent event;
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                event = (ILoggingEvent) objectMessage.getObject();
                Logger log = (Logger) LoggerFactory.getLogger(event.getLoggerName());
                log.callAppenders(event);
            } else {
                logger.warn("Received message is of type " + message.getJMSType() + ", was expecting ObjectMessage.");
            }
        } catch (JMSException jmse) {
            logger.error("Exception thrown while processing incoming message.", jmse);
        }
    }

    protected Object lookup(Context ctx, String name) throws NamingException {
        try {
            return ctx.lookup(name);
        } catch (NameNotFoundException e) {
            logger.error("Could not find name [" + name + "].");
            throw e;
        }
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + JMSQueueSink.class.getName() + " QueueConnectionFactoryBindingName QueueBindingName Username Password");
        System.exit(1);
    }
}
