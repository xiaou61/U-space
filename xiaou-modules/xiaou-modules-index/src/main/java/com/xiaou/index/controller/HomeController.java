package com.xiaou.index.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xiaou.common.domain.R;
import com.xiaou.common.exception.ServiceException;
import com.xiaou.index.domain.entity.SchoolInfo;
import com.xiaou.index.domain.entity.Weather;
import com.xiaou.index.domain.req.SchoolInfoReq;
import com.xiaou.index.domain.resp.SchoolInfoResp;
import com.xiaou.index.service.SchoolInfoService;
import com.xiaou.index.service.impl.WeatherService;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.satoken.constant.RoleConstant;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/school/home")
@Validated
public class HomeController {
    @Resource
    private SchoolInfoService schoolInfoService;
    @Resource
    private WeatherService weatherService;
    /**
     * 更新一下信息
     */
    @PostMapping("/update")
    @SaCheckRole(RoleConstant.ADMIN)
    @Log(title = "更新学校信息", businessType = BusinessType.UPDATE)
    public R<String > updateSchoolInfo(@RequestBody SchoolInfoReq req) {
        return schoolInfoService.updateSchoolInfo(req);
    }
    /**
     * 用户首页查看信息
     */
    @PostMapping("/info")
    public R<SchoolInfoResp> info() {
        return schoolInfoService.info();
    }

    /**
     * 查看当前天气
     */
    @GetMapping("/weather")
    public R<List<Weather>> weather() {
        try {
            return R.ok(weatherService.getWeather("101091101"));
        } catch (IOException e) {
            throw new ServiceException("当前天气获取失败");
        }
    }
}
