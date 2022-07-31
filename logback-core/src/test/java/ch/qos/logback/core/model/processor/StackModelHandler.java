package ch.qos.logback.core.model.processor;

import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.StackModel;

public class StackModelHandler  extends ModelHandlerBase {

    static public final String STACK_TEST = "STACK_TEST"; 
    
    public StackModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new StackModelHandler(context);
    }

    @Override
    protected Class<StackModel> getSupportedModelClass() {
        return StackModel.class;
    }
    
    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        
        StackModel stackModel = (StackModel) model;
        
        String name = stackModel.getName();
        
        ContextBase contextBase = (ContextBase) context;
        
        @SuppressWarnings("unchecked")
        Stack<String> aStack = (Stack) context.getObject(STACK_TEST);
        if(aStack == null) {
            aStack = new Stack<>();
            contextBase.putObject(STACK_TEST, aStack);
        }
        aStack.push(name);
    }
    
    @Override
    public void postHandle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
    }

}
