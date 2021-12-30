/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.model.processor;

import java.util.Map;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;

public class AppenderModelHandler<E> extends ModelHandlerBase {
	Appender<E> appender;
	private boolean inError = false;
	private boolean skipped = false;
	AppenderAttachable<E> appenderAttachable;
	
	public AppenderModelHandler(Context context) {
		super(context);
	}

	@SuppressWarnings("rawtypes")
	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new AppenderModelHandler(context);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void handle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
		this.appender = null;
		this.inError = false;
		
		AppenderModel appenderModel = (AppenderModel) model;

		String appenderName = interpContext.subst(appenderModel.getName());
	
		if(!interpContext.hasDependencies(appenderName)) {
			addWarn("Appender named ["+appenderName+"] not referenced. Skipping further processing.");
			skipped = true;
			appenderModel.markAsSkipped();
			return;
		}
		
//		//this.appenderAttachable = appenderRefBag.get(appenderName);
//		
//		if(this.appenderAttachable == null) {
//			addWarn("Appender named ["+appenderName+"] not referenced. Skipping further processing.");
//			skipped = true;
//			return;
//		}
		
		addInfo("Processing appender named ["+appenderName+"]");
		
		String originalClassName = appenderModel.getClassName();
		String className = interpContext.getImport(originalClassName);
		
		try {
			addInfo("About to instantiate appender of type [" + className + "]");

			appender = (Appender<E>) OptionHelper.instantiateByClassName(className, ch.qos.logback.core.Appender.class,
					context);
			appender.setContext(context);
            appender.setName(appenderName);
			interpContext.pushObject(appender);
		} catch (Exception oops) {
			inError = true;
			addError("Could not create an Appender of type [" + className + "].", oops);
			throw new ModelHandlerException(oops);
		}
	}

	public void postHandle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
		if (inError || skipped) {
			return;
		}
	    if (appender instanceof LifeCycle) {
            ((LifeCycle) appender).start();
        }
        interpContext.markStartOfNamedDependency(appender.getName());
        
        Object o = interpContext.peekObject();

    	@SuppressWarnings("unchecked")
		Map<String, Appender<E>> appenderBag = (Map<String, Appender<E>>) interpContext.getObjectMap()
				.get(JoranConstants.APPENDER_BAG);
    	appenderBag.put(appender.getName(), appender);
    	
        if (o != appender) {
            addWarn("The object at the of the stack is not the appender named [" + appender.getName() + "] pushed earlier.");
        } else {
//        	addInfo("Attaching appender ["+appender.getName()+"] to "+appenderAttachable);
//        	appenderAttachable.addAppender(appender);
        	interpContext.popObject();
        }
    
	}

}
