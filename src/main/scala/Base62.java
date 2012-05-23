import java.math.BigInteger;

/**
 * Base62 encoder.
 *
 * @see http://en.wikipedia.org/wiki/Base_62
 */
public class Base62 {
    public static final BigInteger BASE = BigInteger.valueOf(62);
    public static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String REGEXP = "^[0-9A-Za-z]+$";

    /**
     * Encodes a number using Base62 encoding.
     *
     * @param  number a positive integer
     * @return a Base62 string
     * @throws IllegalArgumentException if <code>number</code> is a negative integer
     */
    public static String encode(BigInteger number) {
        if (number.compareTo(BigInteger.ZERO) == -1) { // number < 0
            throw new IllegalArgumentException("number must not be negative");
        }
        StringBuilder result = new StringBuilder();
        while (number.compareTo(BigInteger.ZERO) == 1) { // number > 0
            BigInteger[] divmod = number.divideAndRemainder(BASE);
            number = divmod[0];
            int digit = divmod[1].intValue();
            result.insert(0, DIGITS.charAt(digit));
        }
        return (result.length() == 0) ? DIGITS.substring(0, 1) : result.toString();
    }

    public static String encode(long number) {
        return encode(BigInteger.valueOf(number));
    }

    /**
     * Decodes a string using Base62 encoding.
     *
     * @param  string a Base62 string
     * @return a positive integer
     * @throws IllegalArgumentException if <code>string</code> is empty
     */
    public static BigInteger decode(final String string) {
        if (string.length() == 0) {
            throw new IllegalArgumentException("string must not be empty");
        }
        BigInteger result = BigInteger.ZERO;
        int digits = string.length();
        for (int index = 0; index < digits; index++) {
            int digit = DIGITS.indexOf(string.charAt(digits - index - 1));
            result = result.add(BigInteger.valueOf(digit).multiply(BASE.pow(index)));
        }
        return result;
    }
}