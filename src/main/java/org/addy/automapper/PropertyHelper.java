package org.addy.automapper;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
			String propertyName = name.length() == 1
					? name.toUpperCase()
					: name.substring(0, 1).toUpperCase() + name.substring(1); // pascalCase(name)
					
			Method getter = findGetter(clazz, propertyName);
			Method setter = findSetter(clazz, propertyName, getter);
            
            if (getter != null && !matchFlags(getter, flags, clazz)) getter = null;
            if (setter != null && !matchFlags(setter, flags, clazz)) setter = null;
            
            if (getter != null || setter != null)
            	return new MethodProperty(getter, setter);
		}
		
		return null;
	}
	
	public static Property getProperty(Class<?> clazz, String name) {
		return getProperty(clazz, name, ALL);
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

	private static void extractFields(Class<?> clazz, int flags, List<Property> properties, Set<String> matchedNames) {
		for (Field field : clazz.getFields()) {
			if (matchFlags(field, flags, clazz)) {
				properties.add(new FieldProperty(field));
				matchedNames.add(field.getName());
			}
		}
	}

	private static void extractEncapsulatedProps(Class<?> clazz, int flags, List<Property> properties, Set<String> matchedNames) {
		for (Method method : clazz.getMethods()) {
			if (matchFlags(method, flags, clazz)) {
				if (isGetter(method)) {
					extractGetterFirst(method, clazz, properties, matchedNames);
				} else if (isSetter(method)) {
					extractSetterFirst(method, clazz, properties, matchedNames);
				}
			}
		}
	}

	private static boolean isGetter(Method method) {
		String methodName = method.getName();
		Class<?> returnType = method.getReturnType();
		
		return method.getParameterCount() == 0 &&
				((methodName.startsWith("get") && returnType != Void.TYPE) ||
				(methodName.startsWith("is") && returnType == Boolean.TYPE) ||
				(methodName.startsWith("has") && returnType == Boolean.TYPE));
	}

	private static boolean isSetter(Method method) {
		return method.getParameterCount() == 1 &&
				method.getReturnType() == Void.TYPE &&
				method.getName().startsWith("set");
	}

	private static void extractGetterFirst(Method getter, Class<?> clazz, List<Property> properties, Set<String> matchedNames) {
		String propName = MethodProperty.toPropertyName(getter.getName());
		
		if (!matchedNames.contains(propName)) {
			String setterName = MethodProperty.toSetterName(getter.getName());
			Method setter = null;
			
			try {
				setter = clazz.getMethod(setterName, getter.getReturnType());
			} catch (NoSuchMethodException | SecurityException e) {
			} finally {
				properties.add(new MethodProperty(getter, setter));
				matchedNames.add(propName);
			}
		}
	}

	private static void extractSetterFirst(Method setter, Class<?> clazz, List<Property> properties, Set<String> matchedNames) {
		String propName = MethodProperty.toPropertyName(setter.getName());
		
		if (!matchedNames.contains(propName)) {
			Method getter = null;
			
		    try {
		        getter = clazz.getMethod("get" + propName);
		    } catch (NoSuchMethodException | SecurityException e1) {
		        Method tmpGetter = null;
		        
		        try {
		            tmpGetter = clazz.getMethod("is" + propName);
		        } catch (NoSuchMethodException | SecurityException e2) {
		            try {
		                tmpGetter = clazz.getMethod("has" + propName);
		            } catch (NoSuchMethodException | SecurityException e3) {
		            }
		        } finally {
		            if (tmpGetter != null && tmpGetter.getReturnType() == Boolean.TYPE)
		                getter = tmpGetter;
		        }
		    } finally {
				properties.add(new MethodProperty(getter, setter));
				matchedNames.add(propName);
		    }
		}
	}

	private static Field findField(Class<?> clazz, String name, int flags) {
		try {
			Field field = clazz.getField(name);
			if (matchFlags(field, flags, clazz)) return field;
		} catch (NoSuchFieldException | SecurityException e) {
		}
		
		return null;
	}

	private static Method findGetter(Class<?> clazz, String propertyName) {
		Method getter = null;
		
		try {
		    getter = clazz.getMethod("get" + propertyName);
		} catch (NoSuchMethodException | SecurityException e1) {
		    Method tmpGetter = null;
		    
		    try {
		        tmpGetter = clazz.getMethod("is" + propertyName);
		    } catch (NoSuchMethodException | SecurityException e2) {
		        try {
		            tmpGetter = clazz.getMethod("has" + propertyName);
		        } catch (NoSuchMethodException | SecurityException e3) {
		        }
		    } finally {
		        if (tmpGetter != null && tmpGetter.getReturnType() == Boolean.TYPE)
		            getter = tmpGetter;
		    }
		}
		
		return getter;
	}

	private static Method findSetter(Class<?> clazz, String propertyName, Method getter) {
		Method setter = null;
		
		if (getter != null) {
		    try {
		        setter = clazz.getMethod("set" + propertyName, getter.getReturnType());
		    } catch (NoSuchMethodException | SecurityException e1) {
		    }
		} else {
			for (Method method : clazz.getMethods()) {
				if (method.getParameterCount() == 1 &&
						method.getReturnType() == Void.TYPE &&
						method.getName().equals("set" + propertyName)) {
					setter = method;
					break;
				}
			}
		}
		
		return setter;
	}

}
