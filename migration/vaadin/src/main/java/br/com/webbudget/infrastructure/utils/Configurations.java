package br.com.webbudget.infrastructure.utils;

import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

public final class Configurations {

    private static final ResourceBundle CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ResourceBundle.getBundle("application");
    }

    public static String get(String configuration) {
        try {
            return CONFIG_PROPERTIES.getString(Objects.requireNonNull(configuration));
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    public static boolean getAsBoolean(String configuration) {
        return Boolean.parseBoolean(Objects.requireNonNull(get(configuration)));
    }

    public static int getAsInteger(String configuration) {
        return Integer.parseInt(Objects.requireNonNull(get(configuration)));
    }
}
