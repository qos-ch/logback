package ch.qos.logback.access.net;

import java.io.IOException;
import java.io.InputStream;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.net.HardenedObjectInputStream;

public class HardenedAccessEventInputStream extends HardenedObjectInputStream {

    public HardenedAccessEventInputStream(InputStream in) throws IOException {
        super(in, new String[] {AccessEvent.class.getName()});
    }

}
