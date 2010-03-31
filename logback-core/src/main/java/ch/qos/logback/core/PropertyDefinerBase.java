package ch.qos.logback.core;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * Set a skeleton implementation for property definers
 * just for have ContextAwareBase.
 *  
 * @author Aleksey Didik
 */
public abstract class PropertyDefinerBase extends ContextAwareBase implements PropertyDefiner {
}
