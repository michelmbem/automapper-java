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
			if (sourceProperties[i] == null) {
				arguments[i] = argumentTypes[i].isPrimitive() ? TypeHelper.defaultValue(argumentTypes[i]) : null;
			} else {
                Object value = sourceProperties[i].getValue(src);
                arguments[i] = value == null || TypeHelper.isAssignable(value.getClass(), argumentTypes[i])
						? value
						: TypeHelper.convertTo(argumentTypes[i], value);
			}

			if (argumentConverters.containsKey(i)) {
				arguments[i] = argumentConverters.get(i).convertArgument(arguments[i], src);
			}
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

}
