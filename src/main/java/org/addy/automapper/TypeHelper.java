package org.addy.automapper;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class TypeHelper {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            char.class, Character.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
    );

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Short.class, short.class,
            Character.class, char.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class
    );

    private static final Map<Couple<Class<?>, Class<?>>, UnaryOperator<Object>> NARROWING_CONVERTERS;

    static {
        // Initialization of NARROWING_CONVERTERS
        //=======================================
        NARROWING_CONVERTERS = new HashMap<>();

        // Primitive -> primitive
        NARROWING_CONVERTERS.put(new Couple<>(short.class, byte.class), value -> (byte)((short)value));

        NARROWING_CONVERTERS.put(new Couple<>(char.class, byte.class), value -> (byte)((char)value));

        NARROWING_CONVERTERS.put(new Couple<>(int.class, byte.class), value -> (byte)((int)value));
        NARROWING_CONVERTERS.put(new Couple<>(int.class, short.class), value -> (short)((int)value));
        NARROWING_CONVERTERS.put(new Couple<>(int.class, char.class), value -> (char)((int)value));

        NARROWING_CONVERTERS.put(new Couple<>(long.class, byte.class), value -> (byte)((long)value));
        NARROWING_CONVERTERS.put(new Couple<>(long.class, short.class), value -> (short)((long)value));
        NARROWING_CONVERTERS.put(new Couple<>(long.class, char.class), value -> (char)((long)value));
        NARROWING_CONVERTERS.put(new Couple<>(long.class, int.class), value -> (int)((long)value));

        NARROWING_CONVERTERS.put(new Couple<>(float.class, byte.class), value -> (byte)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(float.class, short.class), value -> (short)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(float.class, char.class), value -> (char)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(float.class, int.class), value -> (int)((float)value));
        NARROWING_CONVERTERS.put(new Couple<>(float.class, long.class), value -> (long)((float)value));

        NARROWING_CONVERTERS.put(new Couple<>(double.class, byte.class), value -> (byte)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(double.class, short.class), value -> (short)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(double.class, char.class), value -> (char)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(double.class, int.class), value -> (int)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(double.class, long.class), value -> (long)((double)value));
        NARROWING_CONVERTERS.put(new Couple<>(double.class, float.class), value -> (float)((double)value));

        // Reference -> primitive
        NARROWING_CONVERTERS.put(new Couple<>(Short.class, byte.class), value -> ((Short)value).byteValue());

        NARROWING_CONVERTERS.put(new Couple<>(Character.class, byte.class), value -> (byte)((Character)value).charValue());

        NARROWING_CONVERTERS.put(new Couple<>(Integer.class, byte.class), value -> ((Integer)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Integer.class, short.class), value -> ((Integer)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Integer.class, char.class), value -> (char)((Integer)value).intValue());

        NARROWING_CONVERTERS.put(new Couple<>(Long.class, byte.class), value -> ((Long)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Long.class, short.class), value -> ((Long)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Long.class, char.class), value -> (char)((Long)value).longValue());
        NARROWING_CONVERTERS.put(new Couple<>(Long.class, int.class), value -> ((Long)value).intValue());

        NARROWING_CONVERTERS.put(new Couple<>(Float.class, byte.class), value -> ((Float)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, short.class), value -> ((Float)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, char.class), value -> (char)((Float)value).floatValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, int.class), value -> ((Float)value).intValue());
        NARROWING_CONVERTERS.put(new Couple<>(Float.class, long.class), value -> ((Float)value).longValue());

        NARROWING_CONVERTERS.put(new Couple<>(Double.class, byte.class), value -> ((Double)value).byteValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, short.class), value -> ((Double)value).shortValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, char.class), value -> (char)((Double)value).doubleValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, int.class), value -> ((Double)value).intValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, long.class), value -> ((Double)value).longValue());
        NARROWING_CONVERTERS.put(new Couple<>(Double.class, float.class), value -> ((Double)value).floatValue());
    }

    private TypeHelper() {
    }

    public static boolean isAssignable(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null || targetType == null) throw new NullPointerException();
        if (sourceType == targetType) return true;

        // Normal reference assignment
        if (!(sourceType.isPrimitive() || targetType.isPrimitive())) {
            return targetType.isAssignableFrom(sourceType);
        }

        // Both primitive
        if (sourceType.isPrimitive() && targetType.isPrimitive()) {
            return isPrimitiveWideningConvertible(sourceType, targetType);
        }

        // Reference -> primitive: unboxing + optional primitive widening
        if (!sourceType.isPrimitive()) {
            Class<?> unboxed = WRAPPER_TO_PRIMITIVE.get(sourceType);
            if (unboxed == null) return false;
            return isPrimitiveWideningConvertible(unboxed, targetType);
        }

        // Primitive -> reference: boxing + reference widening
        Class<?> boxed = PRIMITIVE_TO_WRAPPER.get(sourceType);
        if (boxed == null) return false;
        return targetType.isAssignableFrom(boxed);
    }

    public static Object defaultValue(Class<?> primitiveType) {
        return Array.get(Array.newInstance(primitiveType, 1), 0);
    }

    public static Object convertTo(Class<?> targetType, Object value) {
        var couple = new Couple<>(value.getClass(), targetType);
        UnaryOperator<Object> converter = NARROWING_CONVERTERS.get(couple);
        if (converter != null) return converter.apply(value);

        Object array = Array.newInstance(targetType, 1);
        Array.set(array, 0, value);
        return Array.get(array, 0);
    }

    private static boolean isPrimitiveWideningConvertible(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == targetType) return true;

        if (sourceType == byte.class) {
            return targetType == short.class
                    || targetType == int.class
                    || targetType == long.class
                    || targetType == float.class
                    || targetType == double.class;
        }

        if (sourceType == short.class || sourceType == char.class) {
            return targetType == int.class
                    || targetType == long.class
                    || targetType == float.class
                    || targetType == double.class;
        }

        if (sourceType == int.class) {
            return targetType == long.class
                    || targetType == float.class
                    || targetType == double.class;
        }

        if (sourceType == long.class) {
            return targetType == float.class
                    || targetType == double.class;
        }

        if (sourceType == float.class) {
            return targetType == double.class;
        }

        return false;
    }

}
