package org.addy.automapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodProperty implements Property {
	
	private final Method getter;
	private final Method setter;
	
	public MethodProperty(Method getter, Method setter) {
		if (getter == null && setter == null)
			throw new IllegalArgumentException("Both getter and setter cannot be null");
		
		this.getter = getter;
		this.setter = setter;
	}
	
	public static String toPropertyName(String methodName) {
		String propertyName;
		
		if (methodName.startsWith("get") || methodName.startsWith("has") || methodName.startsWith("set"))
			propertyName = methodName.substring(3);
		else	// methodName startsWith "is"
			propertyName = methodName.substring(2);
		
		return propertyName.length() == 1
				? propertyName.toLowerCase()
				: propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1); // camelCase(propertyName)
	}
	
	public static String toSetterName(String getterName) {
		return "set" + ((getterName.startsWith("get") || getterName.startsWith("has"))
				? getterName.substring(3)
				: getterName.substring(2));
	}

	@Override
	public Class<?> getType() {
		return getter != null ? getter.getReturnType() : setter.getParameterTypes()[0];
	}

	@Override
	public String getName() {
		return toPropertyName(getter != null ? getter.getName() : setter.getName());
	}

	@Override
	public boolean isReadable() {
		return getter != null;
	}

	@Override
	public boolean isWritable() {
		return setter != null;
	}

	@Override
	public Object getValue(Object target) {
		try {
			return getter.invoke(target);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setValue(Object target, Object value) {
		try {
			setter.invoke(target, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
