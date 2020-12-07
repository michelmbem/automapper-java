package org.addy.automapper;

import java.util.HashMap;
import java.util.Map;

public class Profile {
	
	private static final Map<PropertyCacheKey, Property> propertyCache = new HashMap<>();
	
	private final Map<CoupleOfTypes, Mapping<?, ?>> mappings = new HashMap<>();
	
	public <S, D> Mapping<S, D> createMap(Class<S> sourceClass, Class<D> destClass) {
		Mapping<S, D> mapping = new Mapping<>(sourceClass, destClass);
		CoupleOfTypes key = new CoupleOfTypes(sourceClass, destClass);
		mappings.put(key, mapping);
		return mapping;
	}
	
	public <S, D> boolean hasMap(Class<S> sourceClass, Class<D> destClass) {
		CoupleOfTypes key = new CoupleOfTypes(sourceClass, destClass);
		return mappings.containsKey(key);
	}
	
	@SuppressWarnings("unchecked")
	public <S, D> Mapping<S, D> getMap(Class<S> sourceClass, Class<D> destClass) {
		CoupleOfTypes key = new CoupleOfTypes(sourceClass, destClass);
		return (Mapping<S, D>) mappings.get(key);
	}
	
	public static MappingAction ignore() {
		return (src, srcProp, dest, destProp, ctx) -> {
			// Does nothing!!
		};
	}
	
	public static MappingAction mapFrom(String propName) {
		return (src, srcProp, dest, destProp, ctx) -> {
			Property prop = resolveProperty(src.getClass(), propName);
			if (prop == null) throw new IllegalArgumentException(propName);
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
		PropertyCacheKey key = new PropertyCacheKey(clazz, propName);
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
