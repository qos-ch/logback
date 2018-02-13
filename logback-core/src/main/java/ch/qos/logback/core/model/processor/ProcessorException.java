package ch.qos.logback.core.model.processor;

public class ProcessorException extends Exception {
    private static final long serialVersionUID = 2245242609539650480L;

    public ProcessorException() {
    }

    public ProcessorException(final Throwable rootCause) {
        super(rootCause);
    }
}
