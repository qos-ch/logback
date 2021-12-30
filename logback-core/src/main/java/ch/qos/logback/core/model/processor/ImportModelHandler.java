package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;

public class ImportModelHandler extends ModelHandlerBase {

	public ImportModelHandler(Context context) {
		super(context);
	}

	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new ImportModelHandler(context);
	}	
	
    @Override
    protected Class<ImportModel> getSupportedModelClass() {
    	return ImportModel.class;
    }

    
	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		ImportModel importModel = (ImportModel) model;
		
		String className = importModel.getClassName();
		if(OptionHelper.isNullOrEmpty(className)) {
			addWarn("Empty className not allowed");
			return;
		}
		
		String stem = extractStem(className);
		if(stem == null) {
		  addWarn("["+className+"] could not be imported due to incorrect format");
		  return;
		}
		
		intercon.addImport(stem, className);
		
	}

	String extractStem(String className) {
		if(className == null)
			return null;
		
		int lastDotIndex = className.lastIndexOf(CoreConstants.DOT);
		if(lastDotIndex == -1)
			return null;
		if((lastDotIndex +1) == className.length())
			return null;
		return className.substring(lastDotIndex+1);
	}

}
