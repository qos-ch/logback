package ch.qos.logback.classic.disruptor;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import com.lmax.disruptor.EventFactory;

/**
 * Disruptor Producers to consumers The content of the carrier.
 * 
 * @author Liu-Luke
 */

public class LogValueEvent {
	public LogValueEvent() {

	}
	private ILoggingEvent eventObject;
	private AppenderAttachableImpl<ILoggingEvent> parent;
	public ILoggingEvent getEventObject() {
		return eventObject;
	}
	public void setEventObject(ILoggingEvent eventObject) {
		this.eventObject = eventObject;
	}

	public AppenderAttachableImpl<ILoggingEvent> getParent() {
		return parent;
	}

	public void setParent(AppenderAttachableImpl<ILoggingEvent> parent) {
		this.parent = parent;
	}

	
	/**
	 * Due to the need to let the Disruptor to create an event for us,We also declare a EventFactory to instantiate the Event object.
	 */
	public final static EventFactory<LogValueEvent> EVENT_FACTORY = new EventFactory<LogValueEvent>() {
		@Override
		public LogValueEvent newInstance() {
			return new LogValueEvent();
		}
	};

}
