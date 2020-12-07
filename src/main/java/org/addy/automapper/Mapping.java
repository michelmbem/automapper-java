package org.addy.automapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapping<S, D> {
	
	private static final int FLAGS = PropertyHelper.ALL & ~PropertyHelper.STATIC;
	
	private final Class<S> sourceClass;
	private final Class<D> destClass;
	
	private final List<Property> sourceProperties = new ArrayList<>();
	private final List<Property> destProperties = new ArrayList<>();
	
	private final Map<Property, MappingAction> propertyActions = new HashMap<>();
	
	public Mapping(Class<S> sourceClass, Class<D> destClass) {
		this.sourceClass = sourceClass;
		this.destClass = destClass;
		
		List<Property> destProps = PropertyHelper.getProperties(destClass, FLAGS);
		
		for (Property destProp : destProps) {
			if (destProp.isWritable()) {
				Property srcProp = PropertyHelper.getProperty(sourceClass, destProp.getName(), FLAGS);
				
				if (srcProp != null && srcProp.isReadable()) {
					sourceProperties.add(srcProp);
					destProperties.add(destProp);
					propertyActions.put(destProp, new CopyAction());
				}
			}
		}
	}
	
	public Mapping<S, D> forMember(String memberName, MappingAction action) {
		for (Property destProp : destProperties) {
			if (destProp.getName().equals(memberName)) {
				propertyActions.put(destProp, action);
				return this;
			}
		}

		Property destProp = PropertyHelper.getProperty(destClass, memberName, FLAGS);
		
		if (destProp != null && destProp.isWritable()) {
			Property srcProp = PropertyHelper.getProperty(sourceClass, memberName, FLAGS);
			sourceProperties.add(srcProp != null && srcProp.isReadable() ? srcProp : new EmptyProperty());
			destProperties.add(destProp);
			propertyActions.put(destProp, action);
			return this;
		}
		
		throw new IllegalArgumentException("memberName");
	}
	
	public void apply(S src, D dest, MappingContext ctx) {
		int index = 0;
		for (Property destProp : destProperties) {
			MappingAction action = propertyActions.get(destProp);
			action.execute(src, sourceProperties.get(index), dest, destProp, ctx);
			++index;
		}
	}

}
