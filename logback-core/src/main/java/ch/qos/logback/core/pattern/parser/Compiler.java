/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.pattern.parser;

import java.util.Map;
import java.util.function.Supplier;

import ch.qos.logback.core.pattern.CompositeConverter;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.OptionHelper;

class Compiler<E> extends ContextAwareBase {

    Converter<E> head;
    Converter<E> tail;
    final Node top;
    // converters whose value is a class name
    final Map<String, String> converterMap;
    // converters whose value is a supplier function
    final Map<String, Supplier<Converter<E>>> converterSupplierMap;

    Compiler(final Node top, final Map<String, String> converterMap, Map<String, Supplier<Converter<E>>> converterSupplierMap) {
        this.top = top;
        this.converterMap = converterMap;
        this.converterSupplierMap = converterSupplierMap;
    }

    Converter<E> compile() {
        head = tail = null;
        for (Node n = top; n != null; n = n.next) {
            switch (n.type) {
            case Node.LITERAL:
                addToList(new LiteralConverter<E>((String) n.getValue()));
                break;
            case Node.COMPOSITE_KEYWORD:
                CompositeNode cn = (CompositeNode) n;
                CompositeConverter<E> compositeConverter = createCompositeConverter(cn);
                if (compositeConverter == null) {
                    addError("Failed to create converter for [%" + cn.getValue() + "] keyword");
                    addToList(new LiteralConverter<E>("%PARSER_ERROR[" + cn.getValue() + "]"));
                    break;
                }
                compositeConverter.setFormattingInfo(cn.getFormatInfo());
                compositeConverter.setOptionList(cn.getOptions());
                Compiler<E> childCompiler = new Compiler<E>(cn.getChildNode(), converterMap, converterSupplierMap);
                childCompiler.setContext(context);
                Converter<E> childConverter = childCompiler.compile();
                compositeConverter.setChildConverter(childConverter);
                addToList(compositeConverter);
                break;
            case Node.SIMPLE_KEYWORD:
                SimpleKeywordNode kn = (SimpleKeywordNode) n;
                DynamicConverter<E> dynaConverter = createConverter(kn);
                if (dynaConverter != null) {
                    dynaConverter.setFormattingInfo(kn.getFormatInfo());
                    dynaConverter.setOptionList(kn.getOptions());
                    addToList(dynaConverter);
                } else {
                    // if the appropriate dynaconverter cannot be found, then replace
                    // it with a dummy LiteralConverter indicating an error.
                    Converter<E> errConveter = new LiteralConverter<E>("%PARSER_ERROR[" + kn.getValue() + "]");
                    addStatus(new ErrorStatus("[" + kn.getValue() + "] is not a valid conversion word", this));
                    addToList(errConveter);
                }

            }
        }
        return head;
    }

    private void addToList(Converter<E> c) {
        if (head == null) {
            head = tail = c;
        } else {
            tail.setNext(c);
            tail = c;
        }
    }

    /**
     * Attempt to create a converter using the information found in 'converterMap'.
     *
     * @param kn
     * @return
     */
    @SuppressWarnings("unchecked")
    DynamicConverter<E> createConverter(SimpleKeywordNode kn) {
        DynamicConverter<E> converter = null;
        String keyword = (String) kn.getValue();
        Supplier<Converter<E>> supplier = converterSupplierMap.get(keyword);
        if (supplier != null) {
            // get the value from the supplier
            Converter<E> suppliedValue = supplier.get();
            if (suppliedValue instanceof DynamicConverter) {
                converter = (DynamicConverter<E>)suppliedValue;
            } else {
                addError("Failed to supply converter for keyword [" + keyword + "]");
            }
        } else {
            // create the value from the className
            String converterClassStr = converterMap.get(keyword);
            if (converterClassStr != null) {
                try {
                    converter = (DynamicConverter<E>) OptionHelper.instantiateByClassName(converterClassStr,
                            DynamicConverter.class, context);
                } catch (Exception e) {
                    addError("Failed to instantiate converter class [" + converterClassStr + "] for keyword [" + keyword
                            + "]", e);
                }
            } else {
                addError("There is no conversion registered for conversion word [" + keyword + "]");
            }
        }
        return converter;
    }

    /**
     * Attempt to create a converter using the information found in
     * 'compositeConverterMap'.
     *
     * @param cn
     * @return
     */
    @SuppressWarnings("unchecked")
    CompositeConverter<E> createCompositeConverter(CompositeNode cn) {
        CompositeConverter<E> converter = null;
        String keyword = (String) cn.getValue();
        Supplier<Converter<E>> supplier = converterSupplierMap.get(keyword);
        if (supplier != null) {
            // get the value from the supplier
            Converter<E> suppliedValue = supplier.get();
            if (suppliedValue instanceof CompositeConverter) {
                converter = (CompositeConverter<E>)suppliedValue;
            } else {
                addError("Failed to supply composite converter for keyword [" + keyword + "]");
            }
        } else {
            // create the value from the className
            String converterClassStr = converterMap.get(keyword);
            if (converterClassStr != null) {
                try {
                    converter = (CompositeConverter<E>) OptionHelper.instantiateByClassName(converterClassStr,
                            CompositeConverter.class, context);
                } catch (Exception e) {
                    addError("Failed to instantiate converter class [" + converterClassStr
                            + "] as a composite converter for keyword [" + keyword + "]", e);
                }
            } else {
                addError("There is no conversion registered for composite conversion word [" + keyword + "]");
            }
        }
        return converter;
    }

    // public void setStatusManager(StatusManager statusManager) {
    // this.statusManager = statusManager;
    // }
    //
    // void addStatus(Status status) {
    // if(statusManager != null) {
    // statusManager.add(status);
    // }
    // }
}