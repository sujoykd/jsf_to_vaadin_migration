package br.com.webbudget.infrastructure.utils;

public final class RandomCode {

    private static final String NUMERIC = "1234567890";
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String numeric(int length) {
        return RandomCode.generate(length, RandomCode.NUMERIC);
    }

    public static String alphanumeric(int length) {
        return RandomCode.generate(length, RandomCode.ALPHANUMERIC);
    }

    private static String generate(int length, String baseSequence) {

        long decimalNumber = System.nanoTime();

        int mod;
        int codeLength = 0;

        final StringBuilder builder = new StringBuilder();

        while (decimalNumber != 0 && codeLength < length) {
            mod = (int) (decimalNumber % baseSequence.length());
            builder.append(baseSequence.substring(mod, mod + 1));
            decimalNumber = decimalNumber / baseSequence.length();
            codeLength++;
        }

        return builder.toString();
    }
}
