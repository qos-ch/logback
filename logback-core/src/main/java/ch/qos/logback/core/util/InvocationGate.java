package ch.qos.logback.core.util;

public interface InvocationGate {

    long TIME_UNAVAILABLE = -1;

    /**
     * The caller of this method can decide to skip further work if the returned value is true.
     *
     * Implementations should be able to give a reasonable answer even if  current time date is unavailable.
     *
     * @param currentTime can be TIME_UNAVAILABLE (-1) to signal that time is not available
     * @return if true, caller should skip further work
     */
    boolean isTooSoon(long currentTime);

}