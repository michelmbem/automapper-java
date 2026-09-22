package org.addy.automapper;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoMapper implements MappingContext {
	
	private final Profile profile;
	
	public AutoMapper(Profile profile) {
		if (profile == null)
			throw new IllegalArgumentException("profile cannot be null");
		
		this.profile = profile;
	}

	@SuppressWarnings("unchecked")
	public <S, D> void map(S src, D dest) {
		var mapping = (Mapping<S, D>) profile.getMap(src.getClass(), dest.getClass());
		if (mapping == null) {
			throw new IllegalStateException("No mapping found for " + src.getClass().getName() + " and " + dest.getClass().getName());
		}
		
		mapping.apply(src, dest, this);
	}

	@SuppressWarnings("unchecked")
	public <S, D> D map(S src, Class<D> destClass) {
		if (src == null) return null;

		var mapping = (Mapping<S, D>) profile.getMap(src.getClass(), destClass);
		if (mapping == null) {
			throw new IllegalStateException("No mapping found for " + src.getClass().getName() + " and " + destClass.getName());
		}
		
		D dest = mapping.construct(src);
		mapping.apply(src, dest, this);
		return dest;
	}

	@SuppressWarnings("unchecked")
	public <S, D> D[] map(S[] array, Class<D> destClass) {
		if (array == null) return null;
		
		return Stream.of(array)
				.map(item -> map(item, destClass))
				.filter(Objects::nonNull)
				.toArray(n -> (D[]) Array.newInstance(destClass, n));
	}

	public <S, D> Collection<D> map(Collection<S> collection, Class<D> destClass) {
		if (collection == null) return null;
		
		return collection.stream()
				.map(item -> map(item, destClass))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedList::new));
	}

	public <S, D> List<D> map(List<S> list, Class<D> destClass) {
		if (list == null) return null;
		
		return list.stream()
				.map(item -> map(item, destClass))
				.filter(Objects::nonNull)
				.toList();
	}

	public <S, D> Set<D> map(Set<S> set, Class<D> destClass) {
		if (set == null) return null;
		
		return set.stream()
				.map(item -> map(item, destClass))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	public <S, D, K> Map<K, D> map(Map<K, S> dictionary, Class<D> destClass) {
		if (dictionary == null) return null;
		
		Map<K, D> resDict = new HashMap<>();
		for (Map.Entry<K, S> entry : dictionary.entrySet()) {
			D dest = map(entry.getValue(), destClass);
			if (dest != null) resDict.put(entry.getKey(), dest);
		}
		
		return resDict;
	}

	@Override
	public boolean canMap(Class<?> srcClass, Class<?> destClass) {
		return profile.hasMap(srcClass, destClass);
	}

	@Override
	public Object doMap(Object src, Class<?> destClass) {
		if (src == null) return null;
		if (src instanceof Map<?, ?> dict) return map(dict, destClass);
		if (src instanceof Set<?> set) return map(set, destClass);
		if (src instanceof List<?> list) return map(list, destClass);
		if (src instanceof Collection<?> col) return map(col, destClass);
		if (src.getClass().isArray()) return map((Object[]) src, destClass);
		return map(src, destClass);
	}

}
