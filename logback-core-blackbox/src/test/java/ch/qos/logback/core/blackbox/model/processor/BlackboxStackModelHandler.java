package ch.qos.logback.core.blackbox.model.processor;

import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.blackbox.model.BlackboxStackModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class BlackboxStackModelHandler extends ModelHandlerBase {

    static public final String STACK_TEST = "STACK_TEST"; 
    
    public BlackboxStackModelHandler(Context context) {
        super(context);
    }

    static public BlackboxStackModelHandler makeInstance(Context context, ModelInterpretationContext ic) {
        return new BlackboxStackModelHandler(context);
    }

    @Override
    protected Class<BlackboxStackModel> getSupportedModelClass() {
        return BlackboxStackModel.class;
    }
    
    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        BlackboxStackModel stackModel = (BlackboxStackModel) model;
        
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
