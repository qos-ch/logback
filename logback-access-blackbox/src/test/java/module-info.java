module logback.classic.blackbox {
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires jakarta.mail;
    requires janino;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    exports ch.qos.logback.classic.blackbox.joran;
}