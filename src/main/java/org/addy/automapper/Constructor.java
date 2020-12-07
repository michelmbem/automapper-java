package org.addy.automapper;

@FunctionalInterface
public interface Constructor<S, D> {
	
	D invoke(S src);

}
