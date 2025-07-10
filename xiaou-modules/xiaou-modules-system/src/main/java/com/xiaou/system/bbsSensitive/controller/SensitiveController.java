package com.xiaou.system.bbsSensitive.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.service.SensitiveWordManager;
import com.xiaou.system.bbsSensitive.dto.WordDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 敏感词过滤
 */
@RestController
@RequestMapping("/bbs/sensitive")
@Validated
@SaCheckRole(RoleConstant.BBSADMIN)
public class SensitiveController {
    @Resource
    private SensitiveWordManager sensitiveWordManager;
    /**
     * 新增敏感词
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody WordDto dto) {
        sensitiveWordManager.addWord(dto.getWord(), dto.getLevel());
        return R.ok("添加成功");
    }
    /**
     * 删除敏感词
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestBody String word) {
        sensitiveWordManager.deleteWord(word);
        return R.ok("删除成功");
    }
    /**
     * 列表敏感词
     */
    @PostMapping("/list")
    public R<Map<String, Integer>> list() {
        return R.ok(sensitiveWordManager.listAll());
    }
}
