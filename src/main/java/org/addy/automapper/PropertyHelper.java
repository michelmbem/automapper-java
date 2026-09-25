package org.addy.automapper;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			return new MethodProperty(accessor);
        } catch (NoSuchMethodException e) {
			return null;
        }
    }

	private static Property getEncapsulatedProp(Class<?> clazz, String name, int flags) {
		String propertyName = name.length() == 1
				? name.toUpperCase()
				: name.substring(0, 1).toUpperCase() + name.substring(1); // pascalCase(name)

		Method getter = findGetter(clazz, propertyName);
		Method setter = findSetter(clazz, propertyName, getter);

		if (getter != null && !matchFlags(getter, flags, clazz)) getter = null;
		if (setter != null && !matchFlags(setter, flags, clazz)) setter = null;

        return getter != null || setter != null
				? new MethodProperty(getter, setter)
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

		for (RecordComponent component : clazz.getRecordComponents()) {
			properties.add(new MethodProperty(component.getAccessor()));
			matchedNames.add(component.getName());
		}
	}

	private static void extractEncapsulatedProps(Class<?> clazz, int flags,
												 List<Property> properties,
												 Set<String> matchedNames) {

		for (Method method : clazz.getMethods()) {
			if (matchFlags(method, flags, clazz)) {
				if (MethodHelper.isGetter(method)) {
					extractGetterFirst(method, clazz, properties, matchedNames);
				} else if (MethodHelper.isSetter(method)) {
					extractSetterFirst(method, clazz, properties, matchedNames);
				}
			}
		}
	}

	private static void extractGetterFirst(Method getter, Class<?> clazz,
										   List<Property> properties,
										   Set<String> matchedNames) {

		String propertyName = MethodHelper.toPropertyName(getter.getName());
		if (!matchedNames.contains(propertyName)) {
			String setterName = MethodHelper.toSetterName(getter.getName());
			Method setter = null;

			try {
				setter = clazz.getMethod(setterName, getter.getReturnType());
			} catch (NoSuchMethodException | SecurityException ignored) {
			} finally {
				properties.add(new MethodProperty(getter, setter));
				matchedNames.add(propertyName);
			}
		}
	}

	private static void extractSetterFirst(Method setter, Class<?> clazz,
										   List<Property> properties,
										   Set<String> matchedNames) {

		String propertyName = MethodHelper.toPropertyName(setter.getName());
		if (!matchedNames.contains(propertyName)) {
			Method getter = null;
			
		    try {
		        getter = clazz.getMethod("get" + propertyName);
		    } catch (NoSuchMethodException | SecurityException e1) {
		        Method booleanGetter = null;
		        
		        try {
		            booleanGetter = clazz.getMethod("is" + propertyName);
		        } catch (NoSuchMethodException | SecurityException e2) {
		            try {
		                booleanGetter = clazz.getMethod("has" + propertyName);
		            } catch (NoSuchMethodException | SecurityException ignored) {
		            }
		        } finally {
		            if (booleanGetter != null && booleanGetter.getReturnType() == boolean.class)
		                getter = booleanGetter;
		        }
		    } finally {
				properties.add(new MethodProperty(getter, setter));
				matchedNames.add(propertyName);
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

	private static Method findGetter(Class<?> clazz, String propertyName) {
		Method getter = null;
		
		try {
		    getter = clazz.getMethod("get" + propertyName);
		} catch (NoSuchMethodException | SecurityException e1) {
		    Method booleanGetter = null;
		    
		    try {
		        booleanGetter = clazz.getMethod("is" + propertyName);
		    } catch (NoSuchMethodException | SecurityException e2) {
		        try {
		            booleanGetter = clazz.getMethod("has" + propertyName);
		        } catch (NoSuchMethodException | SecurityException ignored) {
		        }
		    } finally {
		        if (booleanGetter != null && booleanGetter.getReturnType() == boolean.class)
		            getter = booleanGetter;
		    }
		}
		
		return getter;
	}

	private static Method findSetter(Class<?> clazz, String propertyName, Method getter) {
		Method setter = null;
		
		if (getter != null) {
		    try {
		        setter = clazz.getMethod("set" + propertyName, getter.getReturnType());
		    } catch (NoSuchMethodException | SecurityException ignored) {
		    }
		} else {
			for (Method method : clazz.getMethods()) {
				if (method.getParameterCount() == 1
						&& method.getReturnType() == void.class
						&& method.getName().equals("set" + propertyName)) {
					setter = method;
					break;
				}
			}
		}
		
		return setter;
	}

}
