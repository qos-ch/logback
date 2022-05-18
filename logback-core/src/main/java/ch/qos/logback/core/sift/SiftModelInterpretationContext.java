package ch.qos.logback.core.sift;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class SiftModelInterpretationContext extends ModelInterpretationContext {

    public SiftModelInterpretationContext(Context context) {
        super(context);
    }
    
    public SiftModelInterpretationContext(ModelInterpretationContext otherMic) {
        this(otherMic.getContext());
        this.importMap = otherMic.getImportMapCopy();
        this.propertiesMap = otherMic.getCopyOfPropertyMap();
        createAppenderBags();
    } 

    
    @Override
    public boolean hasDependers(String dependeeName) {
        return true;
    }
}
