package ch.qos.logback.access.net.server;

import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.status.Status;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServerSocketAppenderTest {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final MockContext context = new MockContext(executorService);
    {
        context.setStatusManager(new BasicStatusManager());
    }

    private final ServerSocketAppender serverSocketAppender = new ServerSocketAppender();
    {
        serverSocketAppender.setPort(PortFinder.getOpenPort());
        serverSocketAppender.setContext(context);
    }

    @Test
    public void shouldNotFailWhenStopping() throws InterruptedException {
        serverSocketAppender.start(); // start to open a socket
        serverSocketAppender.stop();

        // wait for listener to run async tasks and emit errors
        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        // then
        Assertions.assertThat(getErrors())
                .isEmpty();
    }

    private List<Status> getErrors() {
        return context.getStatusManager()
                .getCopyOfStatusList()
                .stream()
                .filter(status -> status.getLevel() == Status.ERROR)
                .collect(Collectors.toList());
    }
}