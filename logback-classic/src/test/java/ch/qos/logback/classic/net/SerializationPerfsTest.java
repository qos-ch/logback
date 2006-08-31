package ch.qos.logback.classic.net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import ch.qos.logback.classic.net.testObjectBuilders.Builder;
import ch.qos.logback.classic.net.testObjectBuilders.LoggingEvent2Builder;
import ch.qos.logback.classic.net.testObjectBuilders.LoggingEventBuilder;
import ch.qos.logback.classic.net.testObjectBuilders.MinimalExtBuilder;
import ch.qos.logback.classic.net.testObjectBuilders.MinimalSerBuilder;

import junit.framework.TestCase;

public class SerializationPerfsTest extends TestCase {

	ObjectOutputStream oos;

	int loopNumber = 5000;
	int resetFrequency = 100;
	
	/**
	 * Last results:
	 * 
	 * Minimal Object Externalization: average time = 6511 after 10000 writes.
	 * Minimal Object Serialization: average time = 7883 after 10000 writes.
	 * Externalization: average time = 9641 after 10000 writes. Serialization:
	 * average time = 25729 after 10000 writes.
	 */

	public void setUp() throws Exception {
		super.setUp();
		oos = new ObjectOutputStream(new NOPOutputStream());
	}

	public void tearDown() throws Exception {
		super.tearDown();
		oos = null;
	}

	public void testPerf(Builder builder, String label) throws IOException {
		
		// first run for just in time compiler
		int counter = 0;
		for (int i = 0; i < loopNumber; i++) {
			oos.writeObject(builder.build(i));
			oos.flush();
			if (++counter >= resetFrequency) {
				oos.reset();
			}
		}

		// second run
		Long t1;
		Long t2;
		Long total = 0L;
		counter = 0;
		t1 = System.nanoTime();
		for (int i = 0; i < loopNumber; i++) {
			oos.writeObject(builder.build(i));
			oos.flush();
			if (++counter >= resetFrequency) {
				oos.reset();
			}
		}
		t2 = System.nanoTime();
		total += (t2 - t1);
		System.out.println(label+" : average time = "
				+ total / loopNumber + " after " + loopNumber + " writes.");
	}

	public void testWithMinimalExternalization() throws IOException {
		Builder builder = new MinimalExtBuilder();
		testPerf(builder, "Minimal object externalization");
	}

	public void testWithMinimalSerialization() throws IOException {
		Builder builder = new MinimalSerBuilder();
		testPerf(builder, "Minimal object serialization");
	}

	public void testWithExternalization() throws IOException {
		Builder builder = new LoggingEventBuilder();
		testPerf(builder, "LoggingEvent object externalization");		
	}

	public void testWithSerialization() throws IOException {
		Builder builder = new LoggingEvent2Builder();
		testPerf(builder, "LoggingEvent object serialization");	
	}
}
