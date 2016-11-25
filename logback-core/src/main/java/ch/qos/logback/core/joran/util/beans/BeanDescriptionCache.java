package ch.qos.logback.core.joran.util.beans;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 *
 * Cache for {@link BeanDescription} instances. All the cache users which use
 * the same instance of BeanDescriptionCache can profit from each others cached
 * bean descriptions. 
 * 
 * <p>The cache is not thread-safe and should not be shared across configurator instances.
 *
 * @author urechm
 *
 */
public class BeanDescriptionCache extends ContextAwareBase {

    private Map<Class<?>, BeanDescription> classToBeanDescription = new HashMap<Class<?>, BeanDescription>();
    private BeanDescriptionFactory beanDescriptionFactory;

    public BeanDescriptionCache(Context context) {
        setContext(context);
    }

    private BeanDescriptionFactory getBeanDescriptionFactory() {
        if (beanDescriptionFactory == null) {
            beanDescriptionFactory = new BeanDescriptionFactory(getContext());
        }
        return beanDescriptionFactory;
    }

    /**
     * Returned bean descriptions are hold in a cache. If the cache does not
     * contain a description for a given class, a new bean description is
     * created and put in the cache, before it is returned.
     *
     * @param clazz
     *            to get a bean description for.
     * @return a bean description for the given class.
     */
    public BeanDescription getBeanDescription(Class<?> clazz) {
        if (!classToBeanDescription.containsKey(clazz)) {
            BeanDescription beanDescription = getBeanDescriptionFactory().create(clazz);
            classToBeanDescription.put(clazz, beanDescription);
        }
        return classToBeanDescription.get(clazz);
    }

}
