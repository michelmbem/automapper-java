package org.addy.automapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapping<S, D> {
	
	public static final int FLAGS = PropertyHelper.ALL & ~PropertyHelper.STATIC;
	
	private final Class<S> sourceClass;
	private final Class<D> destClass;
	
	private final List<Couple<Property, Property>> properties = new ArrayList<>();
	private final Map<String, MappingAction> propertyActions = new HashMap<>();
	
	private Constructor<S, D> constructor;
	
	public Mapping(Class<S> sourceClass, Class<D> destClass) {
		this.sourceClass = sourceClass;
		this.destClass = destClass;
		this.constructor = new DefaultConstructor<>(destClass);
		
		List<Property> destProps = PropertyHelper.getProperties(destClass, FLAGS);
		MappingAction defaultAction = new CopyAction();
		
		for (Property destProp : destProps) {
			if (destProp.isWritable()) {
				Property srcProp = PropertyHelper.getProperty(sourceClass, destProp.getName(), FLAGS);
				
				if (srcProp != null && srcProp.isReadable()) {
					properties.add(new Couple<>(srcProp, destProp));
					propertyActions.put(destProp.getName(), defaultAction);
				}
			}
		}
	}
	
	public Mapping<S, D> constructUsing(Constructor<S, D> constructor) {
		this.constructor = constructor;
		return this;
	}
	
	public Mapping<S, D> forAllMembers(MappingAction action) {
		properties.clear();
		propertyActions.clear();
		
		List<Property> destProps = PropertyHelper.getProperties(destClass, FLAGS);
		
		for (Property destProp : destProps) {
			if (destProp.isWritable()) {
				Property srcProp = PropertyHelper.getProperty(sourceClass, destProp.getName(), FLAGS);
				if (srcProp == null || !srcProp.isReadable()) srcProp = new EmptyProperty();
				properties.add(new Couple<>(srcProp, destProp));
				propertyActions.put(destProp.getName(), action);
			}
		}
		
		return this;
	}
	
	public Mapping<S, D> forMember(String memberName, MappingAction action) {
		for (Couple<Property, Property> couple : properties) {
			if (couple.getSecond().getName().equals(memberName)) {
				propertyActions.put(memberName, action);
				return this;
			}
		}

		Property destProp = PropertyHelper.getProperty(destClass, memberName, FLAGS);
		
		if (destProp != null && destProp.isWritable()) {
			Property srcProp = PropertyHelper.getProperty(sourceClass, memberName, FLAGS);
			if (srcProp == null || !srcProp.isReadable()) srcProp = new EmptyProperty();
			properties.add(new Couple<>(srcProp, destProp));
			propertyActions.put(memberName, action);
			return this;
		}
		
		throw new IllegalArgumentException(memberName);
	}
	
	public D construct(S src) {
		return constructor.invoke(src);
	}
	
	public void apply(S src, D dest, MappingContext ctx) {
		for (Couple<Property, Property> couple : properties) {
			MappingAction action = propertyActions.get(couple.getSecond().getName());
			action.execute(src, couple.getFirst(), dest, couple.getSecond(), ctx);
		}
	}

}
