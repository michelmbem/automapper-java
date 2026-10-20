package org.addy.automapper;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class RecordConstructor<S, D> implements Constructor<S, D> {

    private final Class<D> targetClass;
	private final Property[] sourceProperties;
	private final Class<?>[] argumentTypes;
	private final Map<Integer, ArgumentConverter<S>> argumentConverters = new HashMap<>();

	public RecordConstructor(Class<S> sourceClass, Class<D> targetClass) {
		Objects.requireNonNull(sourceClass);
        this.targetClass = Objects.requireNonNull(targetClass);

		RecordComponent[] components = targetClass.getRecordComponents();
		sourceProperties = Stream.of(components)
				.map(comp -> PropertyHelper.getProperty(sourceClass, comp.getName()))
				.toArray(Property[]::new);
		argumentTypes = Stream.of(components)
				.map(comp -> comp.getAccessor().getReturnType())
				.toArray(Class<?>[]::new);
	}

	@Override
	public D invoke(S src) {
		var arguments = new Object[argumentTypes.length];
		for (int i = 0; i < arguments.length; ++i) {
			arguments[i] = argumentValue(src, sourceProperties[i], argumentTypes[i], i);
		}

		try {
			return targetClass.getDeclaredConstructor(argumentTypes).newInstance(arguments);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void bindArgumentConverter(int argumentPosition, ArgumentConverter<S> converter) {
		argumentConverters.put(argumentPosition, Objects.requireNonNull(converter));
	}

	private Object argumentValue(S src, Property srcProp, Class<?> argType, int position) {
		Object argValue;

		if (srcProp == null) {
			argValue = argType.isPrimitive() ? TypeHelper.defaultValue(argType) : null;
		} else {
			Object value = srcProp.getValue(src);
			if (value == null)
				argValue = argType.isPrimitive() ? TypeHelper.defaultValue(argType) : null;
			else if (TypeHelper.isAssignable(value.getClass(), argType))
				argValue = value;
			else
				argValue = TypeHelper.convertTo(argType, value);
		}

		if (argumentConverters.containsKey(position))
			argValue = argumentConverters.get(position).convertArgument(argValue, src);

		return argValue;
	}

}
