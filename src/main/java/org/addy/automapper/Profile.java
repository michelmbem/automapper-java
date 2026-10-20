package org.addy.automapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Profile {
	
	private static final Map<Couple<Class<?>, String>, Property> propertyCache = new HashMap<>();
	
	private final Map<Couple<Class<?>, Class<?>>, Mapping<?, ?>> mappings = new HashMap<>();
	
	public <S, D> Mapping<S, D> createMap(Class<S> sourceClass, Class<D> destClass) {
		Objects.requireNonNull(sourceClass);
		Objects.requireNonNull(destClass);

		Mapping<S, D> mapping = new Mapping<>(sourceClass, destClass);
		Couple<Class<?>, Class<?>> key = new Couple<>(sourceClass, destClass);
		mappings.put(key, mapping);

		return mapping;
	}
	
	public <S, D> boolean hasMap(Class<S> sourceClass, Class<D> destClass) {
		Objects.requireNonNull(sourceClass);
		Objects.requireNonNull(destClass);

		Couple<Class<?>, Class<?>> key = new Couple<>(sourceClass, destClass);
		return mappings.containsKey(key) || mappings.keySet()
				.stream()
				.anyMatch(couple -> areAssignable(key, couple));
	}
	
	@SuppressWarnings("unchecked")
	public <S, D> Mapping<S, D> getMap(Class<S> sourceClass, Class<D> destClass) {
		Objects.requireNonNull(sourceClass);
		Objects.requireNonNull(destClass);

		Couple<Class<?>, Class<?>> key = new Couple<>(sourceClass, destClass);
		Mapping<?, ?> mapping = mappings.computeIfAbsent(key,
				couple -> mappings.entrySet()
						.stream()
						.filter(entry -> areAssignable(couple, entry.getKey()))
						.findFirst()
						.map(Map.Entry::getValue)
						.orElse(null));

		return (Mapping<S, D>) mapping;
	}
	
	public static MappingAction ignore() {
		return (src, srcProp, dest, destProp, ctx) -> {
			// Does nothing!!
		};
	}
	
	public static MappingAction mapFrom(String propName) {
		NameHelper.requireNonBlank(propName);

		return (src, srcProp, dest, destProp, ctx) -> {
			Property prop = resolveProperty(src.getClass(), propName);
			CopyAction.copyValue(src, prop, dest, destProp, ctx);
		};
	}

	public static MappingAction mapTo(Class<?> targetClass) {
		Objects.requireNonNull(targetClass);

		return (src, srcProp, dest, destProp, ctx) ->
				destProp.setValue(dest, ctx.doMap(srcProp.getValue(src), targetClass));
	}
	
	public static <T, U> MappingAction convertUsing(Converter<T, U> converter) {
		Objects.requireNonNull(converter);

		return (src, srcProp, dest, destProp, ctx) ->
				destProp.setValue(dest, converter.convert((T) srcProp.getValue(src)));
	}

	public static <T, U> MappingAction convertFrom(String propName, Converter<T, U> converter) {
		NameHelper.requireNonBlank(propName);
		Objects.requireNonNull(converter);

		return (src, srcProp, dest, destProp, ctx) -> {
			Property prop = resolveProperty(src.getClass(), propName);
			destProp.setValue(dest, converter.convert((T) prop.getValue(src)));
		};
	}

	public static <T, U> MappingAction generateUsing(Converter<T, U> converter) {
		Objects.requireNonNull(converter);

		return (src, srcProp, dest, destProp, ctx) ->
				destProp.setValue(dest, converter.convert((T) src));
	}
	
	private static Property resolveProperty(Class<?> clazz, String propName) {
		Property prop = propertyCache.computeIfAbsent(new Couple<>(clazz, propName),
				key -> PropertyHelper.getProperty(clazz, propName, Mapping.FLAGS));
		Objects.requireNonNull(prop, "There is no " + propName + " property in class " + clazz.getName());

		return prop;
	}

	private static boolean areAssignable(Couple<Class<?>, Class<?>> srcCouple,
										 Couple<Class<?>, Class<?>> destCouple) {

		return destCouple.first().isAssignableFrom(srcCouple.first())
				&& destCouple.second().isAssignableFrom(srcCouple.second());
	}

}
