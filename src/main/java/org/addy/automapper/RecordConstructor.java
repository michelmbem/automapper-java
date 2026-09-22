package org.addy.automapper;

import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.util.stream.Stream;

public class RecordConstructor<S, D> implements Constructor<S, D> {

	private final Class<S> sourceClass;
	private final Class<D> targetClass;

	public RecordConstructor(Class<S> sourceClass, Class<D> targetClass) {
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
	}

	@Override
	public D invoke(S src) {
		RecordComponent[] components = targetClass.getRecordComponents();
		Class<?>[] argTypes = Stream.of(components)
				.map(comp -> comp.getAccessor().getReturnType())
				.toArray(Class<?>[]::new);
		Object[] argValues = Stream.of(components)
				.map(comp -> PropertyHelper.getProperty(sourceClass, comp.getName()))
				.map(prop -> prop != null ? prop.getValue(src) : null)
				.toArray(Object[]::new);

		for (int i = 0; i < argValues.length; ++i) {
			if (argValues[i] == null && argTypes[i].isPrimitive()) {
				argValues[i] = defaultValue(argTypes[i]);
			}
		}

		try {
			return targetClass.getDeclaredConstructor(argTypes).newInstance(argValues);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    private static Object defaultValue(Class<?> primitiveType) {
		return Array.get(Array.newInstance(primitiveType, 1), 0);
    }

}
