package com.nd.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码安全工具类：为密码提供"随机盐 + SHA-256"单向加密，避免明文存储。
 *
 * <p>所属模块：healthy-common（公共工具层）。</p>
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
 * <p>关键依赖：被 UserService（注册生成盐密文、登录重算比对、改密换新盐）调用。</p>
 *
 * @author HealthySys 公共模块
 */
public final class PasswordUtil {

    /** 盐的字节长度（16 字节，hex 表示为 32 字符） */
    private static final int SALT_BYTES = 16;
    /** 安全随机数生成器（线程安全，全局共享一个实例即可） */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 工具类私有构造，禁止实例化。
     *
     * <p>本类全部为静态方法，不应通过 new 创建对象。</p>
     */
    private PasswordUtil() {
    }

    /**
     * 生成随机盐（16 字节），以 32 位十六进制字符串返回。
     *
     * <p>每次调用生成不同的盐，确保相同密码在不同账号下密文不同，
     * 防止彩虹表攻击。</p>
     *
     * @return 随机盐的 hex 字符串（32 字符）
     */
    public static String generateSalt() {
        // 分配 16 字节数组作为盐的容器
        byte[] salt = new byte[SALT_BYTES];
        // SecureRandom.nextBytes：用密码学安全随机数填充字节数组，生成不可预测的盐
        RANDOM.nextBytes(salt);
        // 将字节数组转为 32 位十六进制字符串后返回
        return toHex(salt);
    }

    /**
     * 计算密码摘要：SHA-256(salt + password)，以 64 位十六进制字符串返回。
     *
     * <p>拼接顺序为"盐在前、密码在后"，登录/改密校验时必须使用相同拼接顺序重算。</p>
     *
     * @param password 明文密码
     * @param salt     盐（hex 字符串）
     * @return SHA-256 摘要的 hex 字符串（64 字符）
     */
    public static String hash(String password, String salt) {
        try {
            // MessageDigest.getInstance("SHA-256")：获取 SHA-256 摘要算法实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // md.digest(...)：对 "盐+密码" 的 UTF-8 字节执行单向哈希，返回 32 字节摘要
            byte[] digest = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            // 将摘要字节数组转为 64 位十六进制字符串
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
     * @return true=密码正确，false=密码错误（或参数为空）
     */
    public static boolean verify(String password, String salt, String storedHash) {
        // 盐或存储密文为空时直接判定失败，避免 NullPointerException
        if (storedHash == null || salt == null) {
            return false;
        }
        // 用相同盐对输入密码重算摘要，得到本次待比对的密文
        String actual = hash(password, salt);
        // MessageDigest.isEqual：恒定时间比较两个字节数组，避免普通 equals() 因逐字符短路返回而泄露时序信息（防时序攻击）
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将字节数组转为小写十六进制字符串。
     *
     * <p>每字节拆为高 4 位和低 4 位两个十六进制字符，故输出长度 = 字节数 × 2。</p>
     *
     * @param bytes 字节数组
     * @return hex 字符串（小写）
     */
    private static String toHex(byte[] bytes) {
        // 预分配 StringBuilder，容量为字节数 × 2（每字节对应两个 hex 字符）
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            // (b >> 4) & 0xF：取高 4 位转为一个 hex 字符
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            // b & 0xF：取低 4 位转为一个 hex 字符
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
