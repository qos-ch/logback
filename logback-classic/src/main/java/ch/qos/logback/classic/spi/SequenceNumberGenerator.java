package ch.qos.logback.classic.spi;

/**
 * Generate sequence numbers
 * 
 * @since 1.3.0
 * @author Ceki G&uuml;lc&uuml;
 */
public interface SequenceNumberGenerator {

    
    long nextSequenceNumber();
    
}
