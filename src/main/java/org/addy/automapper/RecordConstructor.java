package org.addy.automapper;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RecordConstructor<S, D> implements Constructor<S, D> {

    private final Class<D> targetClass;
	private final Property[] sourceProperties;
	private final Class<?>[] argumentTypes;
	private final Map<Integer, ArgumentConverter<S>> argumentConverters = new HashMap<>();

	public RecordConstructor(Class<S> sourceClass, Class<D> targetClass) {
        this.targetClass = targetClass;

		RecordComponent[] components = targetClass.getRecordComponents();
		sourceProperties = Stream.of(components)
				.map(component -> PropertyHelper.getProperty(sourceClass, component.getName()))
				.toArray(Property[]::new);
		argumentTypes = Stream.of(components)
				.map(component -> component.getAccessor().getReturnType())
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
		argumentConverters.put(argumentPosition, converter);
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
