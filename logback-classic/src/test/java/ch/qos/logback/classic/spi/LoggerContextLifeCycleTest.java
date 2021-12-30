package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StatusChecker;

public class LoggerContextLifeCycleTest {

	
	LoggerContext loggerContext = new LoggerContext();
	Logger logger = loggerContext.getLogger(this.getClass().getName());
	Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
	StatusChecker checker = new StatusChecker(loggerContext);
	int diff = RandomUtil.getPositiveInt();

	void configure(String file) throws JoranException {
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(loggerContext);
		loggerContext.putProperty("diff", "" + diff);
		jc.doConfigure(file);
		loggerContext.start();
	}

	@Test
	public void smoke() throws JoranException {
		configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "spi/contextListener.xml");
		
		List<LoggerContextListener> listenerList = loggerContext.getCopyOfListenerList();
		assertEquals(1, listenerList.size());
		
		ListContextListener lcl = (ListContextListener) listenerList.get(0);
		//lcl.updateList.stream().forEach(System.out::println);
		assertEquals(BasicContextListener.UpdateType.START, lcl.updateList.get(1));
	}
	
	@Test
	public void smokeWithImports() throws JoranException {
		configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "spi/contextListenerWithImports.xml");
		
		List<LoggerContextListener> listenerList = loggerContext.getCopyOfListenerList();
		assertEquals(1, listenerList.size());
		
		ListContextListener lcl = (ListContextListener) listenerList.get(0);
		//lcl.updateList.stream().forEach(System.out::println);
		assertEquals(BasicContextListener.UpdateType.START, lcl.updateList.get(1));
	}

}
