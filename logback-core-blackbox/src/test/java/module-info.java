module ch.qos.logback.core.blackbox {
    requires java.xml;

    requires ch.qos.logback.core;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    requires janino;

    exports ch.qos.logback.core.blackbox.joran.conditional;
    exports ch.qos.logback.core.blackbox.joran;
}