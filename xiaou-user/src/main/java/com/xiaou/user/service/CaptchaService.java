package com.xiaou.user.service;

import java.util.Map;

/**
 * 验证码服务接口
 *
 * @author xiaou
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 验证码信息（包含验证码图片和key）
     */
    Map<String, Object> generateCaptcha();

    /**
     * 验证验证码
     *
     * @param captchaKey 验证码key
     * @param captcha    用户输入的验证码
     * @return 验证结果
     */
    boolean verifyCaptcha(String captchaKey, String captcha);

    /**
     * 删除验证码
     *
     * @param captchaKey 验证码key
     */
    void deleteCaptcha(String captchaKey);
} 