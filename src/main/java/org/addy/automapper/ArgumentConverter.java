package org.addy.automapper;

@FunctionalInterface
public interface ArgumentConverter<T> {

	Object convertArgument(Object argumentValue, T sourceObject);

}
