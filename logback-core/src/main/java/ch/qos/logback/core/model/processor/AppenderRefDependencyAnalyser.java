package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;

public class AppenderRefDependencyAnalyser extends ModelHandlerBase {

	public AppenderRefDependencyAnalyser(Context context) {
		super(context);
	}

    @Override
    protected Class<AppenderRefModel> getSupportedModelClass() {
    	return AppenderRefModel.class;
    }
	
	@Override
	public void handle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
	
		AppenderRefModel appenderRefModel = (AppenderRefModel) model;
		
		String ref = interpContext.subst(appenderRefModel.getRef());
		
		
		Model dependentModel = interpContext.peekModel();
		interpContext.addDependency(dependentModel, ref);
	} 

}
