package ch.qos.logback.core.joran.util.beans;

import java.lang.reflect.Method;

/**
 * Encapsulates utility methods associated with standard java beans.
 * @author urechm
 */
public class BeanUtil {

    //public static final BeanUtil SINGLETON = new BeanUtil();

    public static final String PREFIX_GETTER_IS = "is";
    public static final String PREFIX_GETTER_GET = "get";
    public static final String PREFIX_SETTER = "set";
    public static final String PREFIX_ADDER = "add";

    /**
     *
     * @param method to check if it is an 'adder' method.
     * @return true if the given method is an 'adder' method.
     */
    static public boolean isAdder(Method method) {
        int parameterCount = getParameterCount(method);
        if (parameterCount != 1) {
            return false;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType != void.class) {
            return false;
        }
        String methodName = method.getName();
        return methodName.startsWith(PREFIX_ADDER);
    }

    /**
     *
     * @param method to check if it is a standard java beans getter.
     * @return true if the given method is a standard java beans getter.
     */
    static public boolean isGetter(Method method) {
        int parameterCount = getParameterCount(method);
        if (parameterCount > 0) {
            return false;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            return false;
        }
        String methodName = method.getName();
        if (!methodName.startsWith(PREFIX_GETTER_GET) && !methodName.startsWith(PREFIX_GETTER_IS)) {
            return false;
        }
        if (methodName.startsWith(PREFIX_GETTER_IS)) {
            if (!returnType.equals(boolean.class) && !returnType.equals(Boolean.class)) {
                return false;
            }
        }
        return true;
    }

    static private int getParameterCount(Method method) {
        return method.getParameterTypes().length;
    }

    /**
     *
     * @param method to check if it is a standard java beans setter.
     * @return true if the given method is a standard java beans setter.
     */
    static public boolean isSetter(Method method) {
        int parameterCount = getParameterCount(method);
        if (parameterCount != 1) {
            return false;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType != void.class) {
            return false;
        }
        String methodName = method.getName();
        if (!methodName.startsWith(PREFIX_SETTER)) {
            return false;
        }
        return true;
    }

    /**
     * @param method to get the associated property name for.
     * @return The property name of the associated property if the given method matches a standard java beans getter or setter.
     */
    static public String getPropertyName(Method method) {
        String methodName = method.getName();
        String rawPropertyName = getSubstringIfPrefixMatches(methodName, PREFIX_GETTER_GET);
        if (rawPropertyName == null) {
            rawPropertyName = getSubstringIfPrefixMatches(methodName, PREFIX_SETTER);
        }
        if (rawPropertyName == null) {
            rawPropertyName = getSubstringIfPrefixMatches(methodName, PREFIX_GETTER_IS);
        }
        if (rawPropertyName == null) {
            rawPropertyName = getSubstringIfPrefixMatches(methodName, PREFIX_ADDER);
        }
        return toLowerCamelCase(rawPropertyName);
    }

    /**
     * Converts the given String into lower camel case form.
     * @param string to decapitalize.
     * @return null if the given String is null.
     * Emtpy string if the given string is empty.
     * The given string if the first two consecutive letters are in upper case.
     * The given string with the first letter in lower case otherwise, which might be the given string.
     */
    static public String toLowerCamelCase(String string) {
        if (string == null) {
            return null;
        }
        if (string.isEmpty()) {
            return string;
        }
        if (string.length() > 1 && Character.isUpperCase(string.charAt(1)) && Character.isUpperCase(string.charAt(0))) {
            return string;
        }
        char chars[] = string.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    static private String getSubstringIfPrefixMatches(String wholeString, String prefix) {
        if (wholeString.startsWith(prefix)) {
            return wholeString.substring(prefix.length());
        }
        return null;
    }

}
