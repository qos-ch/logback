package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;

public class AppenderRefDependencyAnalyser extends ModelHandlerBase {

    public AppenderRefDependencyAnalyser(final Context context) {
        super(context);
    }

    @Override
    protected Class<AppenderRefModel> getSupportedModelClass() {
        return AppenderRefModel.class;
    }

    @Override
    public void handle(final InterpretationContext interpContext, final Model model) throws ModelHandlerException {

        final AppenderRefModel appenderRefModel = (AppenderRefModel) model;

        final String ref = interpContext.subst(appenderRefModel.getRef());


        final Model dependentModel = interpContext.peekModel();
        interpContext.addDependency(dependentModel, ref);
    }

}
