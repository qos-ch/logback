package ch.qos.logback.classic.issue.logback1159;

import java.io.IOException;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;

public class LogbackListener extends ContextAwareBase implements StatusListener, LifeCycle {
    private boolean started;

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void addStatusEvent(final Status status) {
        if (status instanceof ErrorStatus && status.getThrowable() instanceof IOException) {
            System.out.println("*************************LogbackListener.addStatusEvent");
            throw new LoggingError(status.getMessage(), status.getThrowable());
        }
    }

}