package com.school.system.common;

import java.security.MessageDigest;
/**
 * MD5 工具类，用于密码加密
 *
 *
 */
public class MD5Utils {
    /**
     * MD5加密方法
     * @param text 明文密码 (如 "123456")
     * @return 加密后的32位十六进制字符串
     */
    public static String encrypt(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String toHex(byte[] bytes) {
        final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }


}