package org.addy.automapper;

import java.util.HashMap;
import java.util.Map;

public class Profile {
	
	private static final Map<Couple<Class<?>, String>, Property> propertyCache = new HashMap<>();
	
	private final Map<Couple<Class<?>, Class<?>>, Mapping<?, ?>> mappings = new HashMap<>();
	
	public <S, D> Mapping<S, D> createMap(Class<S> sourceClass, Class<D> destClass) {
		Mapping<S, D> mapping = new Mapping<>(sourceClass, destClass);
		Couple<Class<?>, Class<?>> key = new Couple<>(sourceClass, destClass);
		mappings.put(key, mapping);
		return mapping;
	}
	
	public <S, D> boolean hasMap(Class<S> sourceClass, Class<D> destClass) {
		Couple<Class<?>, Class<?>> key = new Couple<>(sourceClass, destClass);
		return mappings.containsKey(key) ||
				mappings.keySet().stream().anyMatch(couple ->
					sourceClass.isAssignableFrom(couple.getFirst()) &&
					destClass.isAssignableFrom(couple.getSecond())
				);
	}
	
	@SuppressWarnings("unchecked")
	public <S, D> Mapping<S, D> getMap(Class<S> sourceClass, Class<D> destClass) {
		Couple<Class<?>, Class<?>> key = new Couple<>(sourceClass, destClass);
		Mapping<?, ?> mapping = mappings.get(key);
		
		if (mapping == null) {
			for (Couple<Class<?>, Class<?>> couple : mappings.keySet()) {
				if (sourceClass.isAssignableFrom(couple.getFirst()) &&
						destClass.isAssignableFrom(couple.getSecond())) {
					
					mapping = mappings.get(couple);
					break;
				}
			}
		}
		
		return (Mapping<S, D>) mapping;
	}
	
	public static MappingAction ignore() {
		return (src, srcProp, dest, destProp, ctx) -> {
			// Does nothing!!
		};
	}
	
	public static MappingAction mapFrom(String propName) {
		return (src, srcProp, dest, destProp, ctx) -> {
			Property prop = resolveProperty(src.getClass(), propName);
			if (prop == null) {
				throw new IllegalArgumentException("There is no " + propName + " property in class " + src.getClass().getName());
			}
			
			CopyAction.copyValue(src, prop, dest, destProp, ctx);
		};
	}

	public static MappingAction mapTo(Class<?> targetClass) {
		return (src, srcProp, dest, destProp, ctx) -> {
			destProp.setValue(dest, ctx.doMap(srcProp.getValue(src), targetClass));
		};
	}
	
	public static MappingAction convertUsing(Converter converter) {
		return (src, srcProp, dest, destProp, ctx) -> {
			destProp.setValue(dest, converter.convert(srcProp.getValue(src)));
		};
	}
	
	private static Property resolveProperty(Class<?> clazz, String propName) {
		Couple<Class<?>, String> key = new Couple<>(clazz, propName);
		Property prop;
		
		if (propertyCache.containsKey(key)) {
			prop = propertyCache.get(key);
		} else {
			prop = PropertyHelper.getProperty(clazz, propName, Mapping.FLAGS);
			propertyCache.put(key, prop);
		}
		
		return prop;
	}

}
