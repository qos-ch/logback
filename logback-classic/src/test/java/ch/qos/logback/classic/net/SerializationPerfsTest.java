package ch.qos.logback.classic.net;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent2;

public class SerializationPerfsTest extends TestCase {

	LoggerContext lc;

	int loopNumber = 10000;

	/**
	 * Last results:
	 * 
	 * Minimal Object Externalization: median time = 9981 after 10000 writes.
	 * Minimal Object Serialization: median time = 13127 after 10000 writes.
	 * Externalization: median time = 14008 after 10000 writes. 
	 * Serialization: median time = 31320 after 10000 writes..
	 */

	public void setUp() throws Exception {
		super.setUp();
		lc = new LoggerContext();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		lc = null;
	}

	public void testWithMinimalExternalization() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());

		// first run for just in time compiler
		for (int i = 0; i < loopNumber; i++) {
			oos.writeObject(buildEvent(i));
		}

		// second run
		Long t1;
		Long t2;
		Long total = 0L;
		int resetFrequency = 20;
		int counter = 0;
		for (int i = 0; i < loopNumber; i++) {
			t1 = System.nanoTime();
			oos.writeObject(new MinimalExt(i));
			t2 = System.nanoTime();
			total += (t2 - t1);
			oos.flush();
			if (++counter >= resetFrequency) {
				oos.reset();
			}
		}
		System.out.println("Minimal Object Externalization: median time = " + total
				/ loopNumber + " after " + loopNumber + " writes.");
	}

	public void testWithMinimalSerialization() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());

		// first run for just in time compiler
		for (int i = 0; i < loopNumber; i++) {
			oos.writeObject(buildEvent(i));
		}

		// second run
		Long t1;
		Long t2;
		Long total = 0L;
		int resetFrequency = 20;
		int counter = 0;
		for (int i = 0; i < loopNumber; i++) {
			t1 = System.nanoTime();
			oos.writeObject(new MinimalSer(i));
			t2 = System.nanoTime();
			total += (t2 - t1);
			oos.flush();
			if (++counter >= resetFrequency) {
				oos.reset();
			}
		}
		System.out.println("Minimal Object Serialization: median time = " + total
				/ loopNumber + " after " + loopNumber + " writes.");
	}

	public void testWithExternalization() throws IOException {

		ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());

		// first run for just in time compiler
		for (int i = 0; i < loopNumber; i++) {
			oos.writeObject(buildEvent(i));
		}

		// second run
		Long t1;
		Long t2;
		Long total = 0L;
		int resetFrequency = 20;
		int counter = 0;
		for (int i = 0; i < loopNumber; i++) {
			t1 = System.nanoTime();
			oos.writeObject(buildEvent(i));
			t2 = System.nanoTime();
			total += (t2 - t1);
			oos.flush();
			if (++counter >= resetFrequency) {
				oos.reset();
			}
		}
		System.out.println("Externalization: median time = " + total / loopNumber
				+ " after " + loopNumber + " writes.");
	}

	public void testWithSerialization() throws IOException {

		ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());

		// first run for just in time compiler
		for (int i = 0; i < loopNumber; i++) {
			oos.writeObject(buildEvent2(i));
		}

		// second run
		Long t1;
		Long t2;
		Long total = 0L;
		int resetFrequency = 20;
		int counter = 0;
		for (int i = 0; i < loopNumber; i++) {
			t1 = System.nanoTime();
			oos.writeObject(buildEvent2(i));
			t2 = System.nanoTime();
			total += (t2 - t1);
			oos.flush();
			if (++counter >= resetFrequency) {
				oos.reset();
			}
		}
		System.out.println("Serialization: median time = " + total / loopNumber
				+ " after " + loopNumber + " writes.");
	}

	private LoggingEvent buildEvent(int i) {
		LoggingEvent le = new LoggingEvent();
		le.setLevel(Level.DEBUG);
		le.setLogger(lc.getLogger(LoggerContext.ROOT_NAME));
		// 45 characters message
		le.setMessage("aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa" + i);
		le.setThreadName("threadName");
		return le;
	}

	private LoggingEvent2 buildEvent2(int i) {
		LoggingEvent2 le = new LoggingEvent2();
		le.setLevel(Level.DEBUG);
		le.setLogger(lc.getLogger(LoggerContext.ROOT_NAME));
		// 45 characters message
		le.setMessage("aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa" + i);
		le.setThreadName("threadName");
		return le;
	}
}

class MinimalExt implements Externalizable {

	private static final long serialVersionUID = -1367146218373963709L;

	String message;

	public MinimalExt(int i) {
		// 45 characters message
		message = "aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa" + i;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		message = (String) in.readObject();

	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(message);
	}
}

class MinimalSer implements Serializable {

	private static final long serialVersionUID = 2807646397580899815L;

	String message;

	public MinimalSer(int i) {
		// 45 characters message
		message = "aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa" + i;
	}
}
