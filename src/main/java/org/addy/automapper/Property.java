package org.addy.automapper;

public interface Property {
	
	Class<?> getType();
	String getName();
	boolean isReadable();
	boolean isWritable();
	Object getValue(Object target);
	void setValue(Object target, Object value);

}
