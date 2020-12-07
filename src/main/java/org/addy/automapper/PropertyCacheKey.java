package org.addy.automapper;

public class PropertyCacheKey {
	
	private final Class<?> declaringClass;
	private final String propertyName;
	
	public PropertyCacheKey(Class<?> declaringClass, String propertyName) {
		this.declaringClass = declaringClass;
		this.propertyName = propertyName;
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyCacheKey other = (PropertyCacheKey) obj;
		if (declaringClass == null) {
			if (other.declaringClass != null)
				return false;
		} else if (declaringClass != other.declaringClass)
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}

}
