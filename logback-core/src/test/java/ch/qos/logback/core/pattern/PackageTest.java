package ch.qos.logback.core.pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({SpacePadderTest.class, ch.qos.logback.core.pattern.parser.PackageTest.class})
public class PackageTest  {
}
