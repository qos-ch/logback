package ch.qos.logback.classic.joran.sanity;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IfNestedWithinSecondPhaseElementSCTest {

    LoggerContext context = new LoggerContext();
    IfNestedWithinSecondPhaseElementSC inwspeChecker = new IfNestedWithinSecondPhaseElementSC();
    StatusChecker statusChecker = new StatusChecker(context);

    @BeforeEach
    public void setUp() throws Exception {
        inwspeChecker.setContext(context);
    }

    @Test
    public void smoke() {

        ClassicTopModel topModel = new ClassicTopModel();
        inwspeChecker.check(topModel);
        statusChecker.assertIsWarningOrErrorFree();
    }

    @Test
    public void singleAppender() {
        ClassicTopModel topModel = new ClassicTopModel();
        AppenderModel appenderModel0 = new AppenderModel();
        appenderModel0.setLineNumber(1);
        topModel.addSubModel(appenderModel0);
        inwspeChecker.check(topModel);
        statusChecker.assertIsWarningOrErrorFree();
    }

    @Test
    public void singleLoggerWithNestedIf() {
        ClassicTopModel topModel = new ClassicTopModel();
        Model rootLoggerModel = setupModel(new RootLoggerModel(), "root", 1);
        topModel.addSubModel(rootLoggerModel);

        Model ifModel0 = setupModel(new IfModel(), "if", 2);
        rootLoggerModel.addSubModel(ifModel0);

        Model loggerModel = setupModel(new LoggerModel(), "logger", 3);
        topModel.addSubModel(loggerModel);

        Model ifModel1 = setupModel(new IfModel(), "if", 4);
        loggerModel.addSubModel(ifModel1);

        Model appenderModel = setupModel(new LoggerModel(), "appender", 5);
        topModel.addSubModel(appenderModel);

        Model ifModel2 = setupModel(new IfModel(), "if", 6);
        appenderModel.addSubModel(ifModel2);


        inwspeChecker.check(topModel);
        StatusPrinter.print(context);
        // Element <root> at line 1 contains a nested <if> element at line 2
        String regex0 = "Element <root> at line 1 contains a nested <if> element at line 2";
        statusChecker.assertContainsMatch(Status.WARN, regex0);

        String regex1 = "Element <logger> at line 3 contains a nested <if> element at line 4";
        statusChecker.assertContainsMatch(Status.WARN, regex1);

        String regex2 = "Element <appender> at line 5 contains a nested <if> element at line 6";
        statusChecker.assertContainsMatch(Status.WARN, regex2);

    }



    private Model setupModel(Model m, String tag, int line) {
        m.setLineNumber(line);
        m.setTag(tag);
        return m;
    }


}