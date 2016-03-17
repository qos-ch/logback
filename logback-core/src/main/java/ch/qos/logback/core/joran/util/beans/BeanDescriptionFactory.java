package ch.qos.logback.core.joran.util.beans;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
public class BeanDescriptionFactory {

	public static final BeanDescriptionFactory INSTANCE=new BeanDescriptionFactory();

	private BeanUtil beanUtil=BeanUtil.INSTANCE;

	/**
	 *
	 * @param clazz to create a {@link BeanDescription} for.
	 * @return a {@link BeanDescription} for the given class.
	 */
	public BeanDescription create(Class<?> clazz){
		Map<String, Method> propertyNameToGetter=new HashMap<String, Method>();
		Map<String, Method> propertyNameToSetter=new HashMap<String, Method>();
		Map<String, Method> propertyNameToAdder=new HashMap<String, Method>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if(beanUtil.isGetter(method)){
				String propertyName=beanUtil.getPropertyName(method);
				Method oldGetter = propertyNameToGetter.put(propertyName, method);
				if(oldGetter!=null){
					if(oldGetter.getName().startsWith(BeanUtil.PREFIX_GETTER_IS)){
						propertyNameToGetter.put(propertyName, oldGetter);
					}
					String message=String.format("Warning: Class '%s' contains multiple getters for the same property '%s'.", clazz.getCanonicalName(),propertyName);
					System.err.println(message);
				}
			}else if(beanUtil.isSetter(method)){
				String propertyName=beanUtil.getPropertyName(method);
				Method oldSetter = propertyNameToSetter.put(propertyName, method);
				if(oldSetter!=null){
					String message=String.format("Warning: Class '%s' contains multiple setters for the same property '%s'.", clazz.getCanonicalName(),propertyName);
					System.err.println(message);
				}
			}else if(beanUtil.isAdder(method)){
				String propertyName=beanUtil.getPropertyName(method);
				Method oldAdder = propertyNameToAdder.put(propertyName, method);
				if(oldAdder!=null){
					String message=String.format("Warning: Class '%s' contains multiple adders for the same property '%s'.", clazz.getCanonicalName(),propertyName);
					System.err.println(message);
				}
			}
		}
		return new BeanDescription(clazz,propertyNameToGetter,propertyNameToSetter,propertyNameToAdder);
	}


}
