package org.addy.automapper;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class PropertyHelper {
	
	public static final int DECLARED = 1;
	public static final int INHERITED = 2;
	public static final int STATIC = 4;
	public static final int INSTANCE = 8;
	public static final int FIELD = 16;
	public static final int ENCAPSULATED = 32;
	public static final int ALL = DECLARED | INHERITED | STATIC | INSTANCE | FIELD | ENCAPSULATED;
	
	private PropertyHelper() {
	}
	
	public static List<Property> getProperties(Class<?> clazz, int flags) {
		List<Property> props = new ArrayList<>();
		Set<String> matchedNames = new HashSet<>();
		
		if ((flags & FIELD) != 0) {
			extractFields(clazz, flags, props, matchedNames);
		}
		
		if ((flags & ENCAPSULATED) != 0) {
			if (clazz.isRecord())
				extractRecordComponents(clazz, props, matchedNames);
			else
				extractEncapsulatedProps(clazz, flags, props, matchedNames);
		}
		
		return props;
	}

	public static List<Property> getProperties(Class<?> clazz) {
		return getProperties(clazz, ALL);
	}
	
	public static Property getProperty(Class<?> clazz, String name, int flags) {
		if ((flags & FIELD) != 0) {
			Field field = findField(clazz, name, flags);
			if (field != null) return new FieldProperty(field);
		}
		
		if ((flags & ENCAPSULATED) != 0) {
            return clazz.isRecord()
					? getRecordComponent(clazz, name)
					: getEncapsulatedProp(clazz, name, flags);
        }
		
		return null;
	}

	public static Property getProperty(Class<?> clazz, String name) {
		return getProperty(clazz, name, ALL);
	}

	private static Property getRecordComponent(Class<?> clazz, String name) {
        try {
            Method accessor = clazz.getMethod(name);
			return new MethodProperty(name, accessor, null);
        } catch (NoSuchMethodException e) {
			return null;
        }
    }

	private static Property getEncapsulatedProp(Class<?> clazz, String name, int flags) {
		String suffix = NameHelper.capitalize(name);
		Method getter = findGetter(clazz, suffix);
		Method setter = findSetter(clazz, suffix, getter);

		if (getter != null && !matchFlags(getter, flags, clazz)) getter = null;
		if (setter != null && !matchFlags(setter, flags, clazz)) setter = null;

        return getter != null || setter != null
				? new MethodProperty(name, getter, setter)
				: null;
    }
	
	private static boolean matchFlags(Member member, int flags, Class<?> clazz) {
		int modifiers = member.getModifiers();
		boolean match = Modifier.isPublic(modifiers);
		
		if ((flags & DECLARED) != 0)
			match |= member.getDeclaringClass() == clazz;
		
		if ((flags & INHERITED) != 0)
			match |= member.getDeclaringClass() != clazz;
		
		if ((flags & STATIC) != 0)
			match |= Modifier.isStatic(modifiers);
		
		if ((flags & INSTANCE) != 0)
			match |= !Modifier.isStatic(modifiers);
		
		return match;
	}

	private static void extractFields(Class<?> clazz, int flags,
									  List<Property> properties,
									  Set<String> matchedNames) {

		for (Field field : clazz.getFields()) {
			if (matchFlags(field, flags, clazz)) {
				properties.add(new FieldProperty(field));
				matchedNames.add(field.getName());
			}
		}
	}

	private static void extractRecordComponents(Class<?> clazz,
												List<Property> properties,
												Set<String> matchedNames) {

		for (RecordComponent comp : clazz.getRecordComponents()) {
			properties.add(new MethodProperty(comp.getName(), comp.getAccessor(), null));
			matchedNames.add(comp.getName());
		}
	}

	private static void extractEncapsulatedProps(Class<?> clazz, int flags,
												 List<Property> properties,
												 Set<String> matchedNames) {

		for (Method method : clazz.getMethods()) {
			if (matchFlags(method, flags, clazz)) {
				if (MethodHelper.isGetter(method))
					extractGetterFirst(clazz, properties, matchedNames, method);
				else if (MethodHelper.isSetter(method))
					extractSetterFirst(clazz, properties, matchedNames, method);
			}
		}
	}

	private static Field findField(Class<?> clazz, String name, int flags) {
		try {
			Field field = clazz.getField(name);
			if (matchFlags(field, flags, clazz)) return field;
		} catch (NoSuchFieldException | SecurityException ignored) {
		}
		
		return null;
	}

	private static Method findGetter(Class<?> clazz, String suffix) {
		Method getter = null;
		
		try {
		    getter = clazz.getMethod("get" + suffix);
		} catch (NoSuchMethodException | SecurityException e) {
		    try {
				Method m = clazz.getMethod("is" + suffix);
				if (m.getReturnType() == boolean.class) getter = m;
		    } catch (NoSuchMethodException | SecurityException ignored) {
		    }
		}
		
		return getter;
	}

	private static Method findSetter(Class<?> clazz, String suffix, Method getter) {
		String setterName = "set" + suffix;
		Method setter = null;

		if (getter != null) {
			try {
				setter = clazz.getMethod(setterName, getter.getReturnType());
			} catch (NoSuchMethodException | SecurityException ignored) {
			}
		} else {
			setter = Stream.of(clazz.getMethods())
					.filter(m -> m.getParameterCount() == 1
							&& m.getReturnType() == void.class
							&& m.getName().equals(setterName))
					.findFirst()
					.orElse(null);
		}
		
		return setter;
	}

	private static void extractGetterFirst(Class<?> clazz, List<Property> properties,
										   Set<String> matchedNames, Method getter) {

		String suffix = NameHelper.getSuffix(getter.getName());
		String propertyName = NameHelper.uncapitalize(suffix);

		if (!matchedNames.contains(propertyName)) {
			Method setter = findSetter(clazz, suffix, getter);
			properties.add(new MethodProperty(propertyName, getter, setter));
			matchedNames.add(propertyName);
		}
	}

	private static void extractSetterFirst(Class<?> clazz, List<Property> properties,
										   Set<String> matchedNames, Method setter) {

		String suffix = NameHelper.getSuffix(setter.getName());
		String propertyName = NameHelper.uncapitalize(suffix);

		if (!matchedNames.contains(propertyName)) {
			Method getter = findGetter(clazz, suffix);
			properties.add(new MethodProperty(propertyName, getter, setter));
			matchedNames.add(propertyName);
		}
	}

}
