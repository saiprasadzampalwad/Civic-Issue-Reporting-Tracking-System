/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCryptUtil.java - Password hashing and verification using jBCrypt.
 * Add jbcrypt-0.4.jar to WEB-INF/lib/
 * Download: https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar
 */
public class BCryptUtil {

    private static final int WORK_FACTOR = 10;

    /** Hash a plain-text password. */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Verify a plain-text password against a stored BCrypt hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2")) return false;
        return BCrypt.checkpw(plainPassword, storedHash);
    }
}
