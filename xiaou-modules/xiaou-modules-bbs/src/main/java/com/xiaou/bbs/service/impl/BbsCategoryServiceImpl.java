package com.xiaou.bbs.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.bbs.domain.entity.BbsCategory;
import com.xiaou.bbs.domain.req.BbsCategoryReq;
import com.xiaou.bbs.domain.resp.BbsCategoryResp;
import com.xiaou.bbs.mapper.BbsCategoryMapper;
import com.xiaou.bbs.service.BbsCategoryService;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BbsCategoryServiceImpl extends ServiceImpl<BbsCategoryMapper, BbsCategory>
    implements BbsCategoryService {

    @Override
    public R<String> addCategory(BbsCategoryReq req) {
        //检测有没有相同名称
        QueryWrapper<BbsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", req.getName());
        if (this.count(queryWrapper) > 0) {
            return R.fail("该分类已存在");
        }
        BbsCategory bbsCategory = MapstructUtils.convert(req, BbsCategory.class);
        baseMapper.insert(bbsCategory);
        return R.ok("添加成功");
    }

    @Override
    public R<String> deleteCategory(String id) {
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<String> updateCategory(String id,BbsCategoryReq req) {
        BbsCategory bbsCategory = MapstructUtils.convert(req, BbsCategory.class);
        QueryWrapper<BbsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        if (this.count(queryWrapper) <0){
            return R.fail("该分类不存在");
        }
        baseMapper.update(bbsCategory,queryWrapper);
        return R.ok("更新成功");
    }

    @Override
    public R<List<BbsCategoryResp>> listCategory() {
        List<BbsCategory> list = baseMapper.selectList(null);
        List<BbsCategoryResp> convert = MapstructUtils.convert(list, BbsCategoryResp.class);
        return R.ok(convert);
    }
}




