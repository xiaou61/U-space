package com.xiaou.common.constant;

/**
 * 全局的key常量 (业务无关的key)
 *
 * @author Lion Li
 */
public interface GlobalConstants {

    /**
     * 全局 redis key (业务无关的key)
     */
    String GLOBAL_REDIS_KEY = "global:";

    /**
     * 限流 redis key
     */
    String RATE_LIMIT_KEY = GLOBAL_REDIS_KEY + "rate_limit:";

    /**
     * 0
     */
    Integer ZERO = 0;

    Integer ONE = 1;

    String ALL="All";
    String NOKNOWN = "NOKNOWN";
    String USER_ONLINE_KEY = GLOBAL_REDIS_KEY+"user_online:";

    String GROUPCHECK_KEY = GLOBAL_REDIS_KEY+"group_key:";
}
