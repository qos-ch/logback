package ch.qos.logback.core.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SyslogOutputStream}.
 */
public class SyslogOutputStreamTest {

    @Test
    void connectsToTheSyslogHost() throws Exception {
        try (SyslogOutputStream stream = new SyslogOutputStream("localhost", 1514)) {
            // See #939: the underlying DatagramSocket must be connected to the syslog host so that it
            // does not appear as an unconnected listener on the wildcard address.
            assertTrue(stream.isConnected());
        }
    }

    @Test
    void flushDeliversDatagramToTheSyslogHost() throws Exception {
        try (DatagramSocket receiver = new DatagramSocket(new InetSocketAddress("localhost", 0))) {
            receiver.setSoTimeout(5000);
            int port = receiver.getLocalPort();

            try (SyslogOutputStream stream = new SyslogOutputStream("localhost", port)) {
                byte[] payload = "<14>hello syslog".getBytes(StandardCharsets.UTF_8);
                stream.write(payload, 0, payload.length);
                stream.flush();

                byte[] buffer = new byte[payload.length + 128];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiver.receive(packet);
                byte[] received = Arrays.copyOf(packet.getData(), packet.getLength());

                assertEquals(new String(payload, StandardCharsets.UTF_8), new String(received, StandardCharsets.UTF_8));
            }
        }
    }

}
