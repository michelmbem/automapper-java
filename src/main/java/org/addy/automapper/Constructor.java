package org.addy.automapper;

@FunctionalInterface
public interface Constructor<S, D> {
	
	D invoke(S src);

	default void bindArgumentConverter(int argumentPosition, ArgumentConverter<S> converter) {
		// Does nothing by default
	}

}
