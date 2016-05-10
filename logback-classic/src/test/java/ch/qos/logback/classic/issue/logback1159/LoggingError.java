package ch.qos.logback.classic.issue.logback1159;

/**
 * Based error class to be thrown in a logging failsafe situation. I.e. any unexpected error 
 * situations during logging (e.g. database access, I/O failure)
 * 
 */
public class LoggingError extends Error {

    private static final long serialVersionUID = -4881940499551760472L;

    public LoggingError(String msg, Throwable cause) {
		super(msg, cause);
	}

}