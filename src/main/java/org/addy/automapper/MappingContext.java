package org.addy.automapper;

public interface MappingContext {
	
	boolean canMap(Class<?> srcClass, Class<?> destClass);
	Object doMap(Object src, Class<?> destClass);

}
