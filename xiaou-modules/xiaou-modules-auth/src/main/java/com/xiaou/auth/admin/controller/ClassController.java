package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.domain.excel.ClassEntityExcel;
import com.xiaou.auth.admin.domain.req.ClassReq;
import com.xiaou.auth.admin.domain.resp.ClassResp;
import com.xiaou.auth.admin.service.ClassService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.excel.util.ExcelUtils;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级管理
 */
@RestController
@RequestMapping("/admin/class")
@Validated
public class ClassController {
    @Resource
    private ClassService classService;

    /**
     * 添加班级
     */
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<ClassResp> add(@RequestBody ClassReq req) {
        return classService.add(req);
    }

    /**
     * 删除班级
     */
    @Log(title = "班级管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return classService.delete(id);
    }

    /**
     * 修改班级根据id
     */
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R<ClassResp> update(@RequestBody ClassReq req, @RequestParam String id) {
        return classService.updateClass(req, id);
    }

    /**
     * 分页查看班级
     */
    @PostMapping("/page")
    public R<PageRespDto<ClassResp>> page(@RequestBody PageReqDto req) {
        return classService.pageClass(req);
    }


    /**
     * 导入 Excel（上传文件）
     */
    @Log(title = "班级管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public R<String> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<ClassEntityExcel> dataList = ExcelUtils.read(file.getInputStream(), ClassEntityExcel.class);
            classService.saveBatchFromExcel(dataList);
            return R.ok("导入成功，共导入：" + dataList.size() + " 条数据");
        } catch (Exception e) {
            return R.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导出 Excel
     */
    @Log(title = "班级管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        List<ClassEntityExcel> dataList = classService.getExcelData();
        ExcelUtils.write(response, dataList, ClassEntityExcel.class, "班级信息", "班级表");
    }


    /**
     * 下载 Excel 导入模板
     */
    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        // 创建空数据列表，仅用于生成表头
        List<ClassEntityExcel> emptyList = new ArrayList<>();
        // 调用工具类生成导入模板
        ExcelUtils.write(response, emptyList, ClassEntityExcel.class, "班级导入模板", "导入模板");
    }


}
