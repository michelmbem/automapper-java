package org.addy.automapper;

import java.util.Objects;

public class DefaultConstructor<S, D> implements Constructor<S, D> {
	
	private final Class<D> targetClass;
	
	public DefaultConstructor(Class<D> targetClass) {
		this.targetClass = Objects.requireNonNull(targetClass);
	}

	@Override
	public D invoke(S src) {
		try {
			return targetClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
