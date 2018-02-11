package ch.qos.logback.core.spi;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * A very simple {@link SequenceNumberGenerator} based on an {@link AtomicLong} variable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class BasicSequenceNumberGenerator extends ContextAwareBase implements SequenceNumberGenerator  {

    private final AtomicLong atomicLong = new AtomicLong();
    
    @Override
    public long nextSequenceNumber() {
        return atomicLong.incrementAndGet();
    }

}
