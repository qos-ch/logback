module ch.qos.logback.core { 
    requires static transitive java.sql;
    requires static transitive java.naming;
    requires static transitive java.xml; 
    requires static javax.mail.api;
    requires static javax.servlet.api;
    requires static janino;
    requires static commons.compiler;
    
    
    exports ch.qos.logback.core;
    exports ch.qos.logback.core.boolex;
    exports ch.qos.logback.core.db;

    exports ch.qos.logback.core.encoder;

    exports ch.qos.logback.core.helpers;
    exports ch.qos.logback.core.html;
    
    
    exports ch.qos.logback.core.filter;

    exports ch.qos.logback.core.joran;
    exports ch.qos.logback.core.joran.action;
    exports ch.qos.logback.core.joran.spi;
    exports ch.qos.logback.core.joran.event;  
    exports ch.qos.logback.core.joran.util;
    exports ch.qos.logback.core.joran.conditional;
    
    
    exports ch.qos.logback.core.net;
    exports ch.qos.logback.core.net.server;
    exports ch.qos.logback.core.net.ssl;


    exports ch.qos.logback.core.pattern;
    exports ch.qos.logback.core.pattern.color;
    exports ch.qos.logback.core.pattern.parser;
    
    exports ch.qos.logback.core.sift;
    exports ch.qos.logback.core.spi;
    exports ch.qos.logback.core.status;

    exports ch.qos.logback.core.util;

    
    
  
    
}

