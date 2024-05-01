package fr.supermax_8.endertranslate.core.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class Base64Utils {

    public static String generateSecuredToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[length];
        random.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}