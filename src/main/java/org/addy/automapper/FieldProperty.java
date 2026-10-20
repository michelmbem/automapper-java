package org.addy.automapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class FieldProperty implements Property {
	
	private final Field field;

	public FieldProperty(Field field) {
		this.field = Objects.requireNonNull(field);
	}
	
	public Field getField() {
		return field;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return !Modifier.isFinal(field.getModifiers());
	}

	@Override
	public Object getValue(Object target) {
		try {
			return field.get(target);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object target, Object value) {
		try {
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
