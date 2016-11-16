package ch.qos.logback.classic.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;

/**
 * CustomerHandlerClass.
 * 
 * @author Liu-Luke
 */

public class LogDisruptorEventHandle implements EventHandler<LogValueEvent> {

	public LogDisruptorEventHandle() {

	}
	/**
	 * 
	 * EventHandler.
	 * 
	 * @param event
	 *            event published to the {@link RingBuffer}
	 * @param sequence
	 *            sequence of the event being processed
	 * @param endOfBatch
	 *            endOfBatch flag to indicate if this is the last event in a batch from the {@link RingBuffer} ·
	 * @author Liu-Luke
	 */
	@Override
	public void onEvent(LogValueEvent event, long sequence, boolean endOfBatch) {
		event.getParent().appendLoopOnAppenders(event.getEventObject());
		
	}

}
