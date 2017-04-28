module ch.qos.logback.classic {

	requires org.slf4j;
	requires ch.qos.logback.core;
	
	requires java.xml;
	requires java.management;
	requires java.naming;
	requires static java.sql;
	requires static mail;
	requires static janino;
	requires static javax.servlet.api;
	requires static commons.compiler;
	requires static groovy.all;
	
}