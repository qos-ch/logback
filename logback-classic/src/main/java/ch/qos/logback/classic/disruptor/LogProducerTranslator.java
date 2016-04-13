package ch.qos.logback.classic.disruptor;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import com.lmax.disruptor.EventTranslatorTwoArg;

/**
 * Producers Translator Object.
 * 
 * @author Liu-Luke
 */

public class LogProducerTranslator {


	public static final EventTranslatorTwoArg<LogValueEvent, ILoggingEvent, AppenderAttachableImpl<ILoggingEvent>> TRANSLATOR = new EventTranslatorTwoArg<LogValueEvent, ILoggingEvent, AppenderAttachableImpl<ILoggingEvent>>() {
		@Override
		public void translateTo(LogValueEvent event, long sequence,
				ILoggingEvent arg0, AppenderAttachableImpl<ILoggingEvent> arg1) {
			event.setEventObject(arg0);
			event.setParent(arg1);
		}
	};

}
