package com.nokia.ci.ejb.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SysUserUtil {

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

}
