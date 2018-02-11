package ch.qos.logback.core.spi;

/**
 * Generate sequence numbers
 * 
 * @since 1.3.0
 * @author Ceki G&uuml;lc&uuml;
 */
public interface SequenceNumberGenerator extends ContextAware {

    
    long nextSequenceNumber();
    
}
