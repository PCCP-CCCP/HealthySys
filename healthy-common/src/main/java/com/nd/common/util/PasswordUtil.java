package com.nd.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码安全工具类：为密码提供"随机盐 + SHA-256"单向加密，避免明文存储。
 *
 * <p>存储约定：users 表的 {@code pwd} 列保存 SHA-256(salt + password) 的十六进制摘要（64 位），
 * {@code salt} 列保存随机盐的十六进制（32 位）。登录校验时用相同算法重算并比对，
 * 数据库只存密文，任何情况下不落明文。</p>
 *
 * <p>算法说明：</p>
 * <ul>
 *   <li>{@link #generateSalt()}：使用 {@link SecureRandom} 生成 16 字节随机盐；</li>
 *   <li>{@link #hash(String, String)}：SHA-256(salt + password)，输出 64 位 hex；</li>
 *   <li>{@link #verify(String, String, String)}：重算摘要并与存储值比对，用于登录与改密校验。</li>
 * </ul>
 *
 * @author HealthySys 公共模块
 */
public final class PasswordUtil {

    /** 盐的字节长度（16 字节，hex 表示为 32 字符） */
    private static final int SALT_BYTES = 16;
    /** 安全随机数生成器（线程安全） */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 工具类私有构造，禁止实例化 */
    private PasswordUtil() {
    }

    /**
     * 生成随机盐（16 字节），以 32 位十六进制字符串返回。
     *
     * @return 随机盐的 hex 字符串
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return toHex(salt);
    }

    /**
     * 计算密码摘要：SHA-256(salt + password)，以 64 位十六进制字符串返回。
     *
     * @param password 明文密码
     * @param salt     盐（hex 字符串）
     * @return SHA-256 摘要的 hex 字符串
     */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // JDK 必然内置 SHA-256，此处不会发生，仅作防御性处理
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }

    /**
     * 校验密码是否正确：用相同盐重算摘要并与存储摘要比对（恒定时间比较避免时序侧信道）。
     *
     * @param password   待校验的明文密码
     * @param salt       该用户存储的盐
     * @param storedHash 该用户存储的密码摘要
     * @return true=密码正确，false=密码错误
     */
    public static boolean verify(String password, String salt, String storedHash) {
        if (storedHash == null || salt == null) {
            return false;
        }
        String actual = hash(password, salt);
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将字节数组转为小写十六进制字符串。
     *
     * @param bytes 字节数组
     * @return hex 字符串
     */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
