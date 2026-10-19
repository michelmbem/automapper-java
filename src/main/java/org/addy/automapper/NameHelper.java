package org.addy.automapper;

import java.util.regex.Pattern;

public final class NameHelper {

    private static final Pattern GETTER_NAME = Pattern.compile("^get\\w+$");
    private static final Pattern BOOLEAN_GETTER_NAME = Pattern.compile("^is\\w+$");
    private static final Pattern SETTER_NAME = Pattern.compile("^set\\w+$");

    private NameHelper() {
    }

    public static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String uncapitalize(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static boolean isGetterName(String name) {
        return GETTER_NAME.matcher(name).matches();
    }

    public static boolean isBooleanGetterName(String name) {
        return BOOLEAN_GETTER_NAME.matcher(name).matches();
    }

    public static boolean isSetterName(String name) {
        return SETTER_NAME.matcher(name).matches();
    }

    public static String getSuffix(String name) {
        return name.substring(name.startsWith("is") ? 2 : 3);
    }
}
