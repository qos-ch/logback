package ch.qos.logback.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { ContextBaseTest.class, WriterAppenderTest.class })
public class PackageTest {
}
