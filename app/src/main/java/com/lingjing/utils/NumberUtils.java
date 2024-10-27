package com.lingjing.utils;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：NumberUtils
 * @Date：2024/10/27 上午12:17
 * @Filename：NumberUtils
 * @Version：1.0.0
 */
public class NumberUtils {

    public static boolean isNumeric(String str) {
        return str.matches("^[1-9]\\d*$");
    }

    public static boolean isSixDigitCode(String str) {
        return str.matches("^\\d{6}$");
    }
}
