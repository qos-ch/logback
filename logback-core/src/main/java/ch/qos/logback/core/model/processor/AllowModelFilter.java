package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public class AllowModelFilter implements ModelFiler {

    final Class<? extends Model> allowedModelType;

    AllowModelFilter(final Class<? extends Model> allowedType) {
        allowedModelType = allowedType;
    }

    @Override
    public FilterReply decide(final Model model) {

        if (model.getClass() == allowedModelType) {
            return FilterReply.ACCEPT;
        }

        return FilterReply.NEUTRAL;
    }

}
