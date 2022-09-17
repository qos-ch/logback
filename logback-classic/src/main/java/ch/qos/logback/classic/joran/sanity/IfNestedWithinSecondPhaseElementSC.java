package ch.qos.logback.classic.joran.sanity;

import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.joran.sanity.Pair;
import ch.qos.logback.core.joran.sanity.SanityChecker;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.spi.ContextAwareBase;

import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.core.CoreConstants.CODES_URL;

public class IfNestedWithinSecondPhaseElementSC extends ContextAwareBase implements SanityChecker {

    static final public String NESTED_IF_WARNING_URL = CODES_URL+ "#nested_if_element";

    @Override
    public void check(Model model) {
        if (model == null)
            return;

        List<Model> secondPhaseModels = new ArrayList<>();
        deepFindAllModelsOfType(AppenderModel.class, secondPhaseModels, model);
        deepFindAllModelsOfType(LoggerModel.class, secondPhaseModels, model);
        deepFindAllModelsOfType(RootLoggerModel.class, secondPhaseModels, model);

        List<Pair<Model, Model>> nestedPairs = deepFindNestedSubModelsOfType(IfModel.class, secondPhaseModels);

        if (nestedPairs.isEmpty())
            return;

        addWarn("<if> elements cannot be nested within an <appender>, <logger> or <root> element");
        addWarn("See also " + NESTED_IF_WARNING_URL);
        for (Pair<Model, Model> pair : nestedPairs) {
            Model p = pair.first;
            int pLine = p.getLineNumber();
            Model s = pair.second;
            int sLine = s.getLineNumber();
            addWarn("Element <"+p.getTag()+"> at line " + pLine + " contains a nested <"+s.getTag()+"> element at line " +sLine);
        }
    }

    @Override
    public String toString() {
        return "IfNestedWithinSecondPhaseElementSC";
    }
}
