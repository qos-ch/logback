package ch.qos.logback.core.joran.util.beans;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Lightweight pendant to the java.beans.BeanInfo class. An instance of this
 * class encapsulates the properties of a certain class. The properties are the
 * public setters and getters. In addition the 'add-er'-methods are included,
 * which are the public methods which start with the prefix 'add'.
 *
 * @author urechm
 *
 */
public class BeanDescription {

	private final Class<?> clazz;

	private final Map<String, Method> propertyNameToGetter;

	private final Map<String, Method> propertyNameToSetter;

	private final Map<String, Method> propertyNameToAdder;

	/**
	 * Scope protected since only the {@link BeanDescriptionFactory} must create
	 * BeanDescriptions in order to guarantee consistency between the given
	 * parameters.
	 *
	 * @param clazz of the bean.
	 * @param propertyNameToGetter map of property names to the associated getter.
	 * @param propertyNameToSetter map of property names to the associated setter.
	 * @param propertyNameToAdder map of property names to the associated adder.
	 */
	protected BeanDescription(Class<?> clazz,Map<String, Method> propertyNameToGetter,Map<String, Method> propertyNameToSetter,Map<String, Method> propertyNameToAdder) {
		this.clazz = clazz;
		this.propertyNameToGetter = Collections.unmodifiableMap(propertyNameToGetter);
		this.propertyNameToSetter = Collections.unmodifiableMap(propertyNameToSetter);
		this.propertyNameToAdder = Collections.unmodifiableMap(propertyNameToAdder);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Map<String, Method> getPropertyNameToGetter() {
		return propertyNameToGetter;
	}

	public Map<String, Method> getPropertyNameToSetter() {
		return propertyNameToSetter;
	}

	public Method getGetter(String propertyName) {
		return propertyNameToGetter.get(propertyName);
	}

	public Method getSetter(String propertyName) {
		return propertyNameToSetter.get(propertyName);
	}

	public Map<String, Method> getPropertyNameToAdder() {
		return propertyNameToAdder;
	}

	public Method getAdder(String propertyName) {
		return propertyNameToAdder.get(propertyName);
	}

}
