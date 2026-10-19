package org.addy.automapper;

import java.lang.reflect.Method;

public final class MethodHelper {

    private MethodHelper() {
    }

    public static boolean isGetter(Method method) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        return method.getParameterCount() == 0
                && ((NameHelper.isGetterName(methodName) && returnType != void.class)
                || (NameHelper.isBooleanGetterName(methodName) && returnType == boolean.class));
    }

    public static boolean isSetter(Method method) {
        return method.getParameterCount() == 1
                && method.getReturnType() == void.class
                && NameHelper.isSetterName(method.getName());
    }

}
