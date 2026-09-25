package org.addy.automapper;

import java.lang.reflect.Method;

public class MethodProperty implements Property {

	private final String name;
	private final Method getter;
	private final Method setter;

	public MethodProperty(Method getter, Method setter) {
		if (getter == null && setter == null) throw new NullPointerException();

		name = MethodHelper.toPropertyName(getter != null ? getter.getName() : setter.getName());
		this.getter = getter;
		this.setter = setter;
	}

	public MethodProperty(Method accessor) {
		if (accessor == null) throw new NullPointerException();

		name = accessor.getName();
		getter = accessor;
		setter = null;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	@Override
	public Class<?> getType() {
		return getter != null ? getter.getReturnType() : setter.getParameterTypes()[0];
	}

	@Override
	public String getName() {
		return name;
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object target, Object value) {
		try {
			setter.invoke(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
