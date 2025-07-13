package com.xiaou.secure.annotation;

import java.lang.annotation.*;

/**
 * 在 Controller 方法或类上添加该注解后，将启用参数签名、时间戳校验和 AES 解密校验。
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecureApi {
}