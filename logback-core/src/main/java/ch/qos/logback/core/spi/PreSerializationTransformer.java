package ch.qos.logback.core.spi;

import java.io.Serializable;

/**
 * PreSerializationTransformer instances have the responsibility to transform
 * object into a presumably equivalent serializable representation.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @param <E>
 */
public interface PreSerializationTransformer<E> {
  Serializable transform(E event);
}
