module ch.qos.logback.core {

	requires java.xml;
	requires java.naming;
	requires static java.sql;
	requires static mail;
	requires static janino;
	requires static javax.servlet.api;
	requires static commons.compiler;
	
	exports ch.qos.logback.core;
	exports ch.qos.logback.core.spi;
	exports ch.qos.logback.core.status;
	
	
}