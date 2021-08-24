package ch.epfl.javass.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Class containing utility methods for (de)serializing messages exchanged
 * between client and server
 * 
 * @author Thomas Berkane (297780)
 */
public final class StringSerializer {
    private static final int RADIX = 16;

    /**
     * This is a non-instantiable class
     */
    private StringSerializer() {
    }

    /**
     * Serializes an int to its base 16 representation
     * 
     * @param i
     *            (int): to serialize
     * @return (String): serialized in base 16
     */
    public static String serializeInt(int i) {
        return Integer.toUnsignedString(i, RADIX);
    }

    /**
     * Serializes a long to its base 16 representation
     * 
     * @param i
     *            (long): to serialize
     * @return (String): serialized in base 16
     */
    public static String serializeLong(long l) {
        return Long.toUnsignedString(l, RADIX);
    }

    /**
     * Parses a string representing a base 16 int
     * 
     * @param s
     *            (String): serialized int
     * @return (int): parsed int
     */
    public static int deserializeInt(String s) {
        return Integer.parseUnsignedInt(s, RADIX);
    }

    /**
     * Parses a string representing a base 16 long
     * 
     * @param s
     *            (String): serialized long
     * @return (long): parsed long
     */
    public static long deserializeLong(String s) {
        return Long.parseUnsignedLong(s, RADIX);
    }

    /**
     * Serializes a string encoded in UTF-8 to base64
     * 
     * @param s
     *            (String): to serialize
     * @return (String): serialized in base64
     */
    public static String serializeString(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(bytes);
    }

    /**
     * Parses a string encoded in UTF-8 and serialized in base64
     * 
     * @param s
     *            (String): serialized String
     * @return (String): parsed String
     */
    public static String deserializeString(String s) {
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(s), StandardCharsets.UTF_8);
    }

    /**
     * Combines a variable number of strings into a single string where each
     * individual string is delimited by a particular separator character
     * 
     * @param separator
     *            (char): delimits each string
     * @param s
     *            (String...): strings to combine
     * @return (String): combined strings
     */
    public static String combine(char separator, String... s) {
        return String.join("" + separator, s);
    }

    /**
     * Separates a unique string into an array of strings given a separator
     * character
     * 
     * @param separator
     *            (character): delimits each individual string
     * @param s
     *            (String): string to split
     * @return (String[]): array of individual strings
     */
    public static String[] split(char separator, String s) {
        return s.split("" + separator);
    }

}
