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
			for (Field field : clazz.getFields()) {
				if (matchFlags(field, flags, clazz)) {
					props.add(new FieldProperty(field));
					matchedNames.add(field.getName());
				}
			}
		}
		
		if ((flags & ENCAPSULATED) != 0) {
			for (Method method : clazz.getMethods()) {
				if (matchFlags(method, flags, clazz)) {
					if (isGetter(method)) {
						String propName = MethodProperty.toPropertyName(method.getName());
						
						if (!matchedNames.contains(propName)) {
							String setterName = MethodProperty.toSetterName(method.getName());
							
							try {
								Method setter = clazz.getMethod(setterName, method.getReturnType());
								props.add(new MethodProperty(method, setter));
							} catch (NoSuchMethodException | SecurityException e) {
								props.add(new MethodProperty(method, null));
							} finally {
								matchedNames.add(propName);
							}
						}
					} else if (isSetter(method)) {
						String propName = MethodProperty.toPropertyName(method.getName());
						
						if (!matchedNames.contains(propName)) {
							props.add(new MethodProperty(null, method));
							matchedNames.add(propName);
						}
					}
					
				}
			}
		}
		
		return props;
	}

	public static List<Property> getProperties(Class<?> clazz) {
		return getProperties(clazz, ALL);
	}
	
	public static Property getProperty(Class<?> clazz, String name, int flags) {
		if ((flags & FIELD) != 0) {
			try {
				Field field = clazz.getField(name);
				if (matchFlags(field, flags, clazz))
					return new FieldProperty(field);
			} catch (NoSuchFieldException | SecurityException e) {
			}
		}
		
		if ((flags & ENCAPSULATED) != 0) {
			Method getter = null;
			Method setter = null;
			String propertyName = name.length() == 1
					? name.toUpperCase()
					: name.substring(0, 1).toUpperCase() + name.substring(1); // pascalCase(name)

            try {
                getter = clazz.getMethod("get" + propertyName);
            } catch (NoSuchMethodException | SecurityException ex1) {
                Method tmpGetter = null;
                try {
                    tmpGetter = clazz.getMethod("is" + propertyName);
                } catch (NoSuchMethodException | SecurityException ex2) {
                    try {
                        tmpGetter = clazz.getMethod("has" + propertyName);
                    } catch (NoSuchMethodException | SecurityException ex3) {
                    }
                } finally {
                    if (tmpGetter != null && tmpGetter.getReturnType() == Boolean.TYPE)
                        getter = tmpGetter;
                }
            }

            if (getter != null) {
                try {
                    setter = clazz.getMethod("set" + propertyName, getter.getReturnType());
                } catch (NoSuchMethodException | SecurityException ex1) {
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
            
            if (getter != null && !matchFlags(getter, flags, clazz)) getter = null;
            if (setter != null && !matchFlags(setter, flags, clazz)) setter = null;
            if (getter != null || setter != null) return new MethodProperty(getter, setter);
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

	private static boolean isGetter(Method method) {
		return method.getParameterCount() == 0 &&
				((method.getName().startsWith("get") && method.getReturnType() != Void.TYPE) ||
				(method.getName().startsWith("is") && method.getReturnType() == Boolean.TYPE) ||
				(method.getName().startsWith("has") && method.getReturnType() == Boolean.TYPE));
	}

	private static boolean isSetter(Method method) {
		return method.getParameterCount() == 1 &&
				method.getReturnType() == Void.TYPE &&
				method.getName().startsWith("set");
	}

}
