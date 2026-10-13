package com.zengge.decompiler.mapping;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to normalize and deobfuscate identifiers that use Unicode confusable characters.
 */
public class UnicodeUtils {

    // Small confusable character map (Cyrillic/Greek/fullwidth -> Latin) for common cases
    private static final Map<Character, Character> CONFUSABLES = new HashMap<>();
    static {
        // Cyrillic to Latin
        CONFUSABLES.put('\u0430', 'a'); // Cyrillic small a -> a
        CONFUSABLES.put('\u0410', 'A'); // Cyrillic A
        CONFUSABLES.put('\u0435', 'e'); // Cyrillic small e
        CONFUSABLES.put('\u0415', 'E');
        CONFUSABLES.put('\u043E', 'o'); // Cyrillic o
        CONFUSABLES.put('\u041E', 'O');
        CONFUSABLES.put('\u0441', 'c'); // Cyrillic s -> c
        CONFUSABLES.put('\u0421', 'C');
        CONFUSABLES.put('\u0440', 'p'); // Cyrillic r -> p
        CONFUSABLES.put('\u0420', 'P');
        CONFUSABLES.put('\u0445', 'x'); // Cyrillic kh -> x
        CONFUSABLES.put('\u0425', 'X');
        CONFUSABLES.put('\u0456', 'i'); // Ukrainian i-like
        CONFUSABLES.put('\u0406', 'I');
        CONFUSABLES.put('\u03BF', 'o'); // Greek omicron
        CONFUSABLES.put('\u03B1', 'a'); // Greek alpha
        CONFUSABLES.put('\u03C1', 'p'); // greek rho
        // Fullwidth ASCII variants
        for (char c = '\uFF01'; c <= '\uFF5E'; c++) {
            char mapped = (char)(c - 0xFEE0);
            CONFUSABLES.put(c, mapped);
        }
    }

    /**
     * Suggest an ASCII identifier by normalizing Unicode confusables and removing combining marks.
     * Returns null if no meaningful ASCII equivalent could be produced.
     */
    public static String suggestAsciiIdentifier(String obf) {
        if (obf == null || obf.isEmpty()) return null;

        // First, decompose and remove diacritics
        String normalized = Normalizer.normalize(obf, Normalizer.Form.NFKD);
        StringBuilder sb = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            // skip combining marks
            if (Character.getType(ch) == Character.NON_SPACING_MARK) continue;
            // map confusables
            Character mapped = CONFUSABLES.get(ch);
            if (mapped != null) {
                sb.append(mapped);
            } else {
                // keep ASCII letters/digits/underscore/dot/dollar
                if (ch <= 127) {
                    sb.append(ch);
                } else {
                    // replace other Unicode characters by underscore to preserve positions
                    sb.append('_');
                }
            }
        }

        String candidate = sb.toString();
        // Collapse repeated underscores
        candidate = candidate.replaceAll("_+", "_");
        // trim leading/trailing underscores or dots
        candidate = candidate.replaceAll("^[._]+|[._]+$", "");
        if (candidate.isEmpty()) return null;

        // If candidate contains non-ASCII, give up
        for (int i = 0; i < candidate.length(); i++) {
            if (candidate.charAt(i) > 127) return null;
        }

        // Finally ensure it starts with a valid Java identifier start
        char first = candidate.charAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            // try to prefix an a
            candidate = "a_" + candidate;
        }

        // Ensure remaining chars are Java identifier parts
        StringBuilder finalSb = new StringBuilder();
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c == '.' ) {
                finalSb.append(c);
            } else {
                finalSb.append('_');
            }
        }

        return finalSb.toString();
    }

    /**
     * Quick check whether the string contains characters outside ASCII or known confusables.
     */
    public static boolean likelyObfuscatedWithUnicode(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127) return true;
            if (CONFUSABLES.containsKey(c)) return true;
        }
        return false;
    }
}
