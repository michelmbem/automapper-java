package org.addy.automapper;

import java.lang.reflect.Method;

public final class MethodHelper {

    private MethodHelper() {
    }

    public static boolean isGetter(Method method) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        return method.getParameterCount() == 0
                && ((methodName.startsWith("get") && methodName.length() > 3 && returnType != void.class)
                || (methodName.startsWith("is") && methodName.length() > 2 && returnType == boolean.class)
                || (methodName.startsWith("has") && methodName.length() > 3 && returnType == boolean.class));
    }

    public static boolean isSetter(Method method) {
        String methodName = method.getName();
        return method.getParameterCount() == 1 && method.getReturnType() == void.class
                && methodName.startsWith("set") && methodName.length() > 3;
    }

    public static String toPropertyName(String methodName) {
        String propertyName = methodName.substring(methodName.startsWith("is") ? 2 : 3);
        return propertyName.length() == 1
                ? propertyName.toLowerCase()
                : propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1); // camelCase(propertyName)
    }

    public static String toSetterName(String getterName) {
        return "set" + getterName.substring(getterName.startsWith("is") ? 2 : 3);
    }

}
