package org.addy.automapper;

public class EmptyProperty implements Property {

	@Override
	public Class<?> getType() {
		return Object.class;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public Object getValue(Object target) {
		return null;
	}

	@Override
	public void setValue(Object target, Object value) {
	}

}
