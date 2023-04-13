package ch.qos.logback.access.net.server;

import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.status.Status;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
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

    @Test
    public void shouldNotFailWhenStopping() throws InterruptedException {
        // given
        ServerSocketAppender serverSocketAppender = new ServerSocketAppender();
        serverSocketAppender.setPort(PortFinder.getOpenPort());
        serverSocketAppender.setContext(context);

        // when
        serverSocketAppender.start(); // start to open a socket
        serverSocketAppender.stop();

        waitForListenerToFinishTasks();

        // then
        Assertions.assertThat(getErrors())
                .isEmpty();
    }

    @Test
    public void shouldFailWhenSocketHadAProblem() throws InterruptedException, IOException {
        // given
        int port = PortFinder.getOpenPort();
        try (ServerSocket ignored = new ServerSocket(port)) { // block this port
            ServerSocketAppender serverSocketAppender = new ServerSocketAppender();
            serverSocketAppender.setPort(port);
            serverSocketAppender.setContext(context);

            // when
            serverSocketAppender.start();
            serverSocketAppender.stop();
            waitForListenerToFinishTasks();
        }

        // then
        Assertions.assertThat(getErrors())
                .isNotEmpty();
    }

    private List<Status> getErrors() {
        return context.getStatusManager()
                .getCopyOfStatusList()
                .stream()
                .filter(status -> status.getLevel() == Status.ERROR)
                .collect(Collectors.toList());
    }

    private void waitForListenerToFinishTasks() throws InterruptedException {
        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }
}