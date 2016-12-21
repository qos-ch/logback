package ch.qos.logback.core.appender;

import static org.testng.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import org.testng.annotations.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.ReporterAppender;
import ch.qos.logback.core.encoder.DummyEncoder;
import ch.qos.logback.core.encoder.NopEncoder;
import ch.qos.logback.core.layout.DummyLayout;

public class ReporterAppenderTest extends AbstractAppenderTest<Object> {

	@Override
	protected Appender<Object> getAppender() {
		return new ReporterAppender<Object>();
	}

	@Override
	protected Appender<Object> getConfiguredAppender() {
        ReporterAppender<Object> ra = new ReporterAppender<Object>();
        ra.setEncoder(new NopEncoder<Object>());
        ra.start();
        return ra;
	}

    @Test
    public void smoke() {
        ReporterAppender<Object> ra = (ReporterAppender<Object>) getAppender();
        ra.setEncoder(new DummyEncoder<Object>());
        ra.start();
        ra.doAppend(new Object());
        assertEquals(ra.toString(), DummyLayout.DUMMY);
    }

    @Test
    public void open() {
        ReporterAppender<Object> ra = (ReporterAppender<Object>) getAppender();
        DummyEncoder<Object> dummyEncoder = new DummyEncoder<Object>();
        dummyEncoder.setFileHeader("open");
        ra.setEncoder(dummyEncoder);
        ra.start();
        ra.doAppend(new Object());
        ra.stop();
        assertEquals(ra.toString(), "open" + CoreConstants.LINE_SEPARATOR + DummyLayout.DUMMY);
    }

    @Test
    public void testClose() {
        ReporterAppender<Object> ra = (ReporterAppender<Object>) getAppender();
        DummyEncoder<Object> dummyEncoder = new DummyEncoder<Object>();
        dummyEncoder.setFileFooter("CLOSED");
        ra.setEncoder(dummyEncoder);
        ra.start();
        ra.doAppend(new Object());
        ra.stop();
        assertEquals(ra.toString(), DummyLayout.DUMMY + "CLOSED");
    }

    @Test
    public void testUTF16BE() throws UnsupportedEncodingException {
        ReporterAppender<Object> ra = (ReporterAppender<Object>) getAppender();
        DummyEncoder<Object> dummyEncoder = new DummyEncoder<Object>();
        String encodingName = "UTF-16BE";
        dummyEncoder.setEncodingName(encodingName);
        ra.setEncoder(dummyEncoder);
        ra.start();
        ra.doAppend(new Object());
        assertEquals(new String(ra.toString().getBytes(), encodingName), DummyLayout.DUMMY);
    }
    
    @Test
    @Override
    public void testNewAppender() {
    	super.testNewAppender();
    }
    
    @Test
    @Override
    public void testConfiguredAppender() {
    	super.testConfiguredAppender();
    }
    
    @Test
    @Override
    public void testNoStart() {
    	super.testNoStart();
    }
    
}
