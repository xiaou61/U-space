package com.xiaou.web;

import com.xiaou.common.core.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    @GetMapping("/")
    public Result index() {
        return Result.success("看到这个界面代表你已经启动成功了");
    }
}
