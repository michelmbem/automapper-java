package org.addy.automapper;

import java.util.Collection;
import java.util.Map;

public interface Property {
	
	Class<?> getType();
	String getName();
	boolean isReadable();
	boolean isWritable();
	Object getValue(Object target);
	void setValue(Object target, Object value);
	
	default boolean isCollection() {
		Class<?> type = getType();
		return Collection.class.isAssignableFrom(type) ||
				Map.class.isAssignableFrom(type);
	}

}
