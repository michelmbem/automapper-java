package org.addy.automapper;

public class DefaultConstructor<S, D> implements Constructor<S, D> {
	
	private final Class<D> targetClass;
	
	public DefaultConstructor(Class<D> targetClass) {
		this.targetClass = targetClass;
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
