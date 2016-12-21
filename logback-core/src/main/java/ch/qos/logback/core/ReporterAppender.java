package ch.qos.logback.core;

import java.io.OutputStream;

import org.testng.ITestResult;
import org.testng.Reporter;

public class ReporterAppender<E> extends OutputStreamAppender<E> {

	protected boolean logToStdOut = false;
	
    @Override
    public void start() {
    	ReporterOutputStream ros = new ReporterOutputStream();
    	ros.setLogToStdOut(logToStdOut);
    	super.setOutputStream(ros);
        super.start();
    }
    
    @Override
    protected void subAppend(E event) {
        if (started) {
            super.subAppend(event);
			((ReporterOutputStream) getOutputStream()).flush();
        }
    }
    
    @Override
    public void setOutputStream(OutputStream outputStream) {
    	throw new UnsupportedOperationException("The output stream of " + this.getClass().getName() + " cannot be altered");
    }
    
    @Override
    public String toString() {
    	ITestResult testResult = Reporter.getCurrentTestResult();
    	return String.join("", Reporter.getOutput(testResult));
    }

	/**
	 * Specify if output should be sent to <i>STDOUT</i> in addition to the TestNG HTML report.
	 * @param logToStdOut {@code false} to send output only to the TestNG HTML report; 
	 *         {@code true} to fork output to <i>STDOUT</i> and the TestNG HTML report.
	 */
	public void setLogToStdOut(boolean logToStdOut) {
		this.logToStdOut = logToStdOut;
		ReporterOutputStream ros = (ReporterOutputStream) getOutputStream();
		if (ros != null) {
			ros.setLogToStdOut(logToStdOut);
		}
	}
	
	/**
	 * Determine if output is being forked to <i>STDOUT</i> and the TestNG HTML report.
	 * @return {@code false} if output is being sent solely to the TestNG HTML report; 
	 *         {@code true} if output is being forked to <i>STDOUT</i> and the TestNG HTML report.
	 */
	public boolean doLogToStdOut() {
		return logToStdOut;
	}

}
