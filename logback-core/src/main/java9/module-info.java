module ch.qos.logback.core {
    requires transitive java.xml;
    requires static java.sql;

    // required by the optional SMTPAppenderBase component
    requires static java.naming;

    requires static janino;
    requires static commons.compiler;

    // transitive _imposes_ the presence of javax.mail on downstream users,
    // let them declare it if they need it
    requires static javax.mail.api;

    //  javax.servlet:javax.servlet-api:jar:4.0.1:compile -> auto-named as javax.servlet.api
    requires static javax.servlet.api;

    exports ch.qos.logback.core;
    exports ch.qos.logback.core.boolex;
    
    exports ch.qos.logback.core.encoder;

    exports ch.qos.logback.core.helpers;
    exports ch.qos.logback.core.html;
    
    exports ch.qos.logback.core.filter;

    exports ch.qos.logback.core.joran;
    exports ch.qos.logback.core.joran.action;
    exports ch.qos.logback.core.joran.conditional;
    exports ch.qos.logback.core.joran.event;
    exports ch.qos.logback.core.joran.sanity;
    exports ch.qos.logback.core.joran.spi;
    exports ch.qos.logback.core.joran.util;
    exports ch.qos.logback.core.joran.util.beans;

    exports ch.qos.logback.core.model;
    exports ch.qos.logback.core.model.conditional;
    exports ch.qos.logback.core.model.processor;
    exports ch.qos.logback.core.model.processor.conditional;
    exports ch.qos.logback.core.model.util;

    exports ch.qos.logback.core.net;
    exports ch.qos.logback.core.net.server;
    exports ch.qos.logback.core.net.ssl;

    exports ch.qos.logback.core.pattern;
    exports ch.qos.logback.core.pattern.color;
    exports ch.qos.logback.core.pattern.parser;

    exports ch.qos.logback.core.recovery;
    exports ch.qos.logback.core.read;
    exports ch.qos.logback.core.rolling;
    exports ch.qos.logback.core.rolling.helper;

    exports ch.qos.logback.core.sift;
    exports ch.qos.logback.core.spi;
    exports ch.qos.logback.core.status;

    exports ch.qos.logback.core.testUtil;
    exports ch.qos.logback.core.util;

}

