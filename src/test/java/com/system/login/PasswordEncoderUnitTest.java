package com.system.login;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器单元测试 - 不需要Spring上下文
 * @author xwj
 * @version 1.0
 */
public class PasswordEncoderUnitTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void testPasswordMatches() {
        // **** 请在这里替换为您的实际数据 ****
        String rawPasswordInput = "admin"; // <-- 替换为你在前端尝试登录时输入的密码
        String encodedPasswordFromDB = "$2a$10$7SFDUykS1r0vPZVenmGNs.fba6bBPaZOg.19k/GfMc5p.PG1Oo7FW"; // <-- 替换为从数据库或日志中获取的加密密码
        // ************************************

        System.out.println("原始密码: " + rawPasswordInput);
        System.out.println("加密密码 (DB): " + encodedPasswordFromDB);

        // 执行匹配
        boolean isMatch = passwordEncoder.matches(rawPasswordInput, encodedPasswordFromDB);

        System.out.println("匹配结果: " + isMatch);

        // 根据期望的结果进行断言（可选，但推荐）
        // 如果期望密码匹配成功，使用 assertTrue
        // assertTrue(isMatch, "密码应该匹配");

        // 如果期望密码不匹配，使用 assertFalse
        // assertFalse(isMatch, "密码不应该匹配");

        // 为了方便查看结果，这里不强制断言，只打印结果
        // 但在正式单元测试中，应该使用断言
    }

    @Test
    public void testEncodePassword() {
        // 可选：测试如何生成一个新密码的 BCrypt 散列值
        String newRawPassword = "admin"; // <-- 替换为你想要加密的密码
        String newEncodedPassword = passwordEncoder.encode(newRawPassword);
        System.out.println("新原始密码: " + newRawPassword);
        System.out.println("新加密密码: " + newEncodedPassword);
    }
}