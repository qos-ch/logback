package ch.qos.logback.core.joran.sanity;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.TopModel;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.qos.logback.core.joran.sanity.AppenderWithinAppenderSanityChecker.NESTED_APPENDERS_WARNING;

public class AppenderWithinAppenderSanityCheckerTest {


    Context context = new ContextBase();
    AppenderWithinAppenderSanityChecker awasc = new AppenderWithinAppenderSanityChecker();
    StatusChecker statusChecker = new StatusChecker(context);

    @BeforeEach
    public void setUp() throws Exception {
        awasc.setContext(context);
    }

    @Test
    public void smoke() {

        TopModel topModel = new TopModel();
        awasc.check(topModel);
        statusChecker.assertIsWarningOrErrorFree();
    }


    @Test
    public void singleAppender() {
        TopModel topModel = new TopModel();
        AppenderModel appenderModel0 = new AppenderModel();
        appenderModel0.setLineNumber(1);
        topModel.addSubModel(appenderModel0);
        awasc.check(topModel);
        statusChecker.assertIsWarningOrErrorFree();
    }

    @Test
    public void nestedAppender() {
        TopModel topModel = new TopModel();
        AppenderModel appenderModel0 = new AppenderModel();
        appenderModel0.setLineNumber(1);
        topModel.addSubModel(appenderModel0);

        AppenderModel appenderModel1 = new AppenderModel();
        appenderModel1.setLineNumber(2);
        appenderModel0.addSubModel(appenderModel1);

        awasc.check(topModel);

        statusChecker.assertContainsMatch(Status.WARN, NESTED_APPENDERS_WARNING);
        statusChecker.assertContainsMatch(Status.WARN,"Appender at line 1");
    }

}