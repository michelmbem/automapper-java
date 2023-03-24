package org.addy.automapper;

@FunctionalInterface
public interface Converter<T, U> {

	U convert(T value);

}
