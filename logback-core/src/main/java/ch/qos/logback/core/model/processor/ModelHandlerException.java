package ch.qos.logback.core.model.processor;

public class ModelHandlerException extends Exception {

    private static final long serialVersionUID = -6486247349285796564L;

    public ModelHandlerException() {
    }

    public ModelHandlerException(final Throwable rootCause) {
        super(rootCause);
    }
}
