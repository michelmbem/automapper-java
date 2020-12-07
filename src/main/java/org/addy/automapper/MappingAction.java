package org.addy.automapper;

@FunctionalInterface
public interface MappingAction {
	
	void execute(Object src, Property srcProp, Object dest, Property destProp, MappingContext ctx);

}
