package org.addy.automapper;

public class CoupleOfTypes {
	
	private final Class<?> firstType;
	private final Class<?> secondType;
	
	public CoupleOfTypes(Class<?> firstType, Class<?> secondType) {
		this.firstType = firstType;
		this.secondType = secondType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstType == null) ? 0 : firstType.hashCode());
		result = prime * result + ((secondType == null) ? 0 : secondType.hashCode());
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
		CoupleOfTypes other = (CoupleOfTypes) obj;
		if (firstType == null) {
			if (other.firstType != null)
				return false;
		} else if (firstType != other.firstType)
			return false;
		if (secondType == null) {
			if (other.secondType != null)
				return false;
		} else if (secondType != other.secondType)
			return false;
		return true;
	}

}
