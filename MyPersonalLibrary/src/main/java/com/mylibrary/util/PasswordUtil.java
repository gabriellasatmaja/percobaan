package com.mylibrary.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing and verification.
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {}

    /** Hash a plain-text password. */
    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Return true if the plain-text password matches the stored hash. */
    public static boolean verify(String plaintext, String hash) {
        if (plaintext == null || hash == null) return false;
        try {
            return BCrypt.checkpw(plaintext, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
