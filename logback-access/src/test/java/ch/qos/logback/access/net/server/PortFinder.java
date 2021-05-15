package ch.qos.logback.access.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public class PortFinder {
    private static final Random RANDOM = new Random();
    private static final int PORT_MIN = 1 << 10;
    private static final int PORT_MAX = (1 << 16) - 1;

    private PortFinder() {
        // util
    }

    public static int getOpenPort() {
        while(true) {
            int port = getRandomPort();
            try(ServerSocket ignored = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                // port is closed
            }
        }
    }

    public static int getRandomPort() {
        return RANDOM.nextInt(PORT_MAX - PORT_MIN) + PORT_MIN;
    }
}
