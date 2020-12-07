package org.addy.automapper;

@FunctionalInterface
public interface Converter {
	
	Object convert(Object value);

}
