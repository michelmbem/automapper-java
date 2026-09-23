package org.addy.automapper;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class TypeHelper {

    private static final Map<Couple<Class<?>, Class<?>>, Function<Object, Object>> NARROWING_CONVERTERS;

    static {
        NARROWING_CONVERTERS = new HashMap<>();

        // Primitive type to primitive type

        NARROWING_CONVERTERS.put(new Couple<>(Short.TYPE, Byte.TYPE), value -> (byte)((short)value));

        NARROWING_CONVERTERS.put(new Couple<>(Integer.TYPE, Byte.TYPE), value -> (byte)((int)value));
        NARROWING_CONVERTERS.put(new Couple<>(Integer.TYPE, Short.TYPE), value -> (short)((int)value));

        NARROWING_CONVERTERS.put(new Couple<>(Long.TYPE, Byte.TYPE), value -> (byte)((long)value));
        NARROWING_CONVERTERS.put(new Couple<>(Long.TYPE, Short.TYPE), value -> (short)((long)value));
        NARROWING_CONVERTERS.put(new Couple<>(Long.TYPE, Integer.TYPE), value -> (int)((long)value));

        NARROWING_CONVERTERS.put(new Couple<>(Float.TYPE, Byte.TYPE), value -> (byte)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(Float.TYPE, Short.TYPE), value -> (short)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(Float.TYPE, Integer.TYPE), value -> (int)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(Float.TYPE, Long.TYPE), value -> (long)((float)value));

        NARROWING_CONVERTERS.put(new Couple<>(Double.TYPE, Byte.TYPE), value -> (byte)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(Double.TYPE, Short.TYPE), value -> (short)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(Double.TYPE, Integer.TYPE), value -> (int)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(Double.TYPE, Long.TYPE), value -> (long)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(Double.TYPE, Float.TYPE), value -> (float)((double)value));

        // Class to primitive type

        NARROWING_CONVERTERS.put(new Couple<>(Short.class, Byte.TYPE), value -> ((Number)value).byteValue());

        NARROWING_CONVERTERS.put(new Couple<>(Integer.class, Byte.TYPE), value -> ((Number)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Integer.class, Short.TYPE), value -> ((Number)value).shortValue());

        NARROWING_CONVERTERS.put(new Couple<>(Long.class, Byte.TYPE), value -> ((Number)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Long.class, Short.TYPE), value -> ((Number)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Long.class, Integer.TYPE), value -> ((Number)value).intValue());

        NARROWING_CONVERTERS.put(new Couple<>(Float.class, Byte.TYPE), value -> ((Number)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, Short.TYPE), value -> ((Number)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, Integer.TYPE), value -> ((Number)value).intValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, Long.TYPE), value -> ((Number)value).longValue());

        NARROWING_CONVERTERS.put(new Couple<>(Double.class, Byte.TYPE), value -> ((Number)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, Short.TYPE), value -> ((Number)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, Integer.TYPE), value -> ((Number)value).intValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, Long.TYPE), value -> ((Number)value).longValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, Float.TYPE), value -> ((Number)value).floatValue());
    }

    private TypeHelper() {
    }

    public static Object defaultValue(Class<?> primitiveType) {
        return Array.get(Array.newInstance(primitiveType, 1), 0);
    }

    public static Object convert(Class<?> targetType, Object value) {
        Object array = Array.newInstance(targetType, 1);

        try {
            Array.set(array, 0, value);
        } catch (IllegalArgumentException e) {
            var couple = new Couple<>(value.getClass(), targetType);
            if (!NARROWING_CONVERTERS.containsKey(couple)) throw e;
            Array.set(array, 0, NARROWING_CONVERTERS.get(couple).apply(value));
        }

        return Array.get(array, 0);
    }

}
