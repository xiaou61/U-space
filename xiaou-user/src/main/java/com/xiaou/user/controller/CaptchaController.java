package com.xiaou.user.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.user.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 验证码控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码
     */
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateCaptcha() {
        try {
            Map<String, Object> captcha = captchaService.generateCaptcha();
            log.debug("生成验证码成功，key: {}", captcha.get("captchaKey"));
            return Result.success("验证码生成成功", captcha);
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return Result.error("验证码生成失败");
        }
    }

    /**
     * 验证验证码（测试接口，生产环境可以移除）
     */
    @PostMapping("/verify")
    public Result<Boolean> verifyCaptcha(@RequestParam String captchaKey, @RequestParam String captcha) {
        try {
            boolean isValid = captchaService.verifyCaptcha(captchaKey, captcha);
            return Result.success(isValid ? "验证码正确" : "验证码错误", isValid);
        } catch (Exception e) {
            log.error("验证验证码失败，key: {}", captchaKey, e);
            return Result.error("验证码验证失败");
        }
    }
} 