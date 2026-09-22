package br.com.webbudget.infrastructure.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class Utilities {

    public static String decimalToString(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value);
    }
}
