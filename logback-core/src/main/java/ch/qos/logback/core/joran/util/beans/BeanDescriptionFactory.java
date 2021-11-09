package ch.qos.logback.core.joran.util.beans;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * Encapsulates creation of {@link BeanDescription} instances.
 * This factory is kind of a lightweight Introspector as described in the Java Beans API specification.
 * The given class is only analyzed for its public getters, setters and adders methods.
 * Implementations of the BeanInfo interface are not taken into account for analysis.
 * Therefore this class is only partially compatible with the Java Beans API specification.
 *
 *
 * @author urechm
 */
public class BeanDescriptionFactory extends ContextAwareBase {

    BeanDescriptionFactory(final Context context) {
        setContext(context);
    }

    /**
     *
     * @param clazz to create a {@link BeanDescription} for.
     * @return a {@link BeanDescription} for the given class.
     */
    public BeanDescription create(final Class<?> clazz) {
        final Map<String, Method> propertyNameToGetter = new HashMap<>();
        final Map<String, Method> propertyNameToSetter = new HashMap<>();
        final Map<String, Method> propertyNameToAdder = new HashMap<>();
        final Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            if(method.isBridge()) {
                // we can safely ignore bridge methods
                continue;
            }
            if (BeanUtil.isGetter(method)) {
                final String propertyName = BeanUtil.getPropertyName(method);
                final Method oldGetter = propertyNameToGetter.put(propertyName, method);
                if (oldGetter != null) {
                    if (oldGetter.getName().startsWith(BeanUtil.PREFIX_GETTER_IS)) {
                        propertyNameToGetter.put(propertyName, oldGetter);
                    }
                    final String message = String.format("Class '%s' contains multiple getters for the same property '%s'.", clazz.getCanonicalName(), propertyName);
                    addWarn(message);
                }
            } else if (BeanUtil.isSetter(method)) {
                final String propertyName = BeanUtil.getPropertyName(method);
                final Method oldSetter = propertyNameToSetter.put(propertyName, method);
                if (oldSetter != null) {
                    final String message = String.format("Class '%s' contains multiple setters for the same property '%s'.", clazz.getCanonicalName(), propertyName);
                    addWarn(message);
                }
            } else if (BeanUtil.isAdder(method)) {
                final String propertyName = BeanUtil.getPropertyName(method);
                final Method oldAdder = propertyNameToAdder.put(propertyName, method);
                if (oldAdder != null) {
                    final String message = String.format("Class '%s' contains multiple adders for the same property '%s'.", clazz.getCanonicalName(), propertyName);
                    addWarn(message);
                }
            }
        }
        return new BeanDescription(clazz, propertyNameToGetter, propertyNameToSetter, propertyNameToAdder);
    }
}
