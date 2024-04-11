package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.model.processor.DefaultProcessor;

public interface NewRuleProvider {

    void addPathActionAssociations(RuleStore rs);
    void addModelHandlerAssociations(DefaultProcessor defaultProcessor);
    void addModelAnalyserAssociations(DefaultProcessor defaultProcessor);
}
