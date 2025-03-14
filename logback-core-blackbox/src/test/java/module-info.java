module ch.qos.logback.core.blackbox {

    requires ch.qos.logback.core;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    requires janino;
    requires org.fusesource.jansi;

    requires org.tukaani.xz;

    exports ch.qos.logback.core.blackbox.joran.conditional;
    exports ch.qos.logback.core.blackbox.joran;
    exports ch.qos.logback.core.blackbox.appender;
    exports ch.qos.logback.core.blackbox.rolling.helper;

}