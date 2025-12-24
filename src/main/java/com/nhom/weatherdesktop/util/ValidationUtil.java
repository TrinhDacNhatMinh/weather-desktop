package com.nhom.weatherdesktop.util;

import java.math.BigDecimal;

public class ValidationUtil {
    
    public static BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static boolean equalValues(BigDecimal v1, BigDecimal v2) {
        if (v1 == null && v2 == null) return true;
        if (v1 == null || v2 == null) return false;
        return v1.compareTo(v2) == 0;
    }
}
