package com.xiaou.auth;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class Test {

    /**
     * xxl-job 任务处理器
     */
    @XxlJob("demoJobHandler")
    public ReturnT<String> testHandler(String param) {
        System.out.println("xxl-job test 执行，参数为：" + param);
        return ReturnT.SUCCESS;
    }
}
