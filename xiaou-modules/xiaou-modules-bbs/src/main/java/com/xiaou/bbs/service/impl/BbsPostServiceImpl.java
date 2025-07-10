package com.xiaou.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.bbs.domain.entity.BbsPost;
import com.xiaou.bbs.domain.entity.BbsPostLike;
import com.xiaou.bbs.domain.req.BbsPostReq;
import com.xiaou.bbs.domain.resp.BbsPostResp;
import com.xiaou.bbs.domain.resp.BbsStudentInfoResp;
import com.xiaou.bbs.mapper.BbsPostLikeMapper;
import com.xiaou.bbs.mapper.BbsPostMapper;
import com.xiaou.bbs.service.BbsPostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.upload.utils.FilesUtils;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BbsPostServiceImpl extends ServiceImpl<BbsPostMapper, BbsPost>
        implements BbsPostService {

    @Resource
    private LoginHelper loginHelper;

    @Resource
    private StudentMapper studentMapper;
    @Autowired
    private FilesUtils filesUtils;
    @Resource
    private BbsPostLikeMapper bbsPostLikeMapper;

    @Override
    public R<String> add(BbsPostReq req) {
        BbsPost post = MapstructUtils.convert(req, BbsPost.class);
        post.setUserId(loginHelper.getCurrentAppUserId());
        baseMapper.insert(post);
        //todo 制作关键词过滤系统 如果有敏感词则不允许发布
        //todo 发送消息推送

        return R.ok("添加成功");
    }

    @Override
    public R<String> delete(String id) {
        //判断该帖子是不是自己的帖子
        if (!baseMapper.selectById(id).getUserId().equals(loginHelper.getCurrentAppUserId())) {
            return R.fail("这个帖子不是你的无法删除");
        }
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<String> deleteAdmin(String id) {
        baseMapper.deleteById(id);
        //todo 后续需要通知用户
        return R.ok("删除成功");
    }

    //todo 这里后面也会做一个缓存，就是缓存帖子信息，然后用户打开帖子时，会去Redis里面查找，如果存在那么就直接返回，不存在那么就入库查询
    //todo 这里也会有一个问题，就是当有点赞等业务的时候，需要及时的去更新帖子
    @Override
    public R<PageRespDto<BbsPostResp>> listPost(String categoryId, PageReqDto reqDto) {

        IPage<BbsPost> page = new Page<>(reqDto.getPageNum(), reqDto.getPageSize());

        QueryWrapper<BbsPost> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, reqDto, List.of("created_at"));
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }

        IPage<BbsPost> bbsPostIPage = baseMapper.selectPage(page, queryWrapper);

        List<BbsPostResp> convert = MapstructUtils.convert(bbsPostIPage.getRecords(), BbsPostResp.class);

        //todo 这里会优化 即当用户打开帖子的时候，会缓存自己的信息到Redis当中，这里可以先去看Redis里面有没有。如果没有的话那么在入库去查找

        // ✅ 1. 批量提取 userId
        Set<String> userIds = convert.stream()
                .map(BbsPostResp::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // ✅ 2. 批量查询 student 信息
        if (!userIds.isEmpty()) {
            List<Student> students = studentMapper.selectBatchIds(userIds);
            Map<String, BbsStudentInfoResp> userMap = students.stream()
                    .collect(Collectors.toMap(
                            Student::getId,
                            s -> MapstructUtils.convert(s, BbsStudentInfoResp.class)
                    ));

            // ✅ 3. 统一填充 userInfo
            for (BbsPostResp post : convert) {
                post.setUserInfo(userMap.get(post.getUserId()));
            }
        }

        return R.ok(PageRespDto.of(
                bbsPostIPage.getCurrent(),
                bbsPostIPage.getSize(),
                bbsPostIPage.getTotal(),
                convert
        ));
    }

    @Override
    public R<PageRespDto<BbsPostResp>> listPostMy(PageReqDto reqDto) {
        IPage<BbsPost> page = new Page<>(reqDto.getPageNum(), reqDto.getPageSize());
        QueryWrapper<BbsPost> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, reqDto, List.of("created_at"));
        queryWrapper.eq("user_id", loginHelper.getCurrentAppUserId());
        IPage<BbsPost> bbsPostIPage = baseMapper.selectPage(page, queryWrapper);
        List<BbsPostResp> convert = MapstructUtils.convert(bbsPostIPage.getRecords(), BbsPostResp.class);
        for (BbsPostResp post : convert) {
            post.setUserInfo(MapstructUtils.convert(studentMapper.selectById(post.getUserId()), BbsStudentInfoResp.class));
        }
        return R.ok(PageRespDto.of(bbsPostIPage.getCurrent(),
                bbsPostIPage.getSize(),
                bbsPostIPage.getTotal(),
                convert));
    }

    @Override
    public R<List<String>> uploadImage(MultipartFile[] file) {
        try {
            List<FileInfo> fileInfos = filesUtils.uploadFiles(file);
            List<String> urls = fileInfos.stream()
                    .map(FileInfo::getUrl)
                    .toList();
            return R.ok("上传成功", urls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public R<String> viewPost(String id) {
        BbsPost post = baseMapper.selectById(id);
        if (post != null) {
            // 浏览数加一
            baseMapper.updateViewCountById(id, 1);
            return R.ok("成功");
        }
        return R.fail("帖子不存在");
    }

    @Override
    public R<String> likePost(String id) {
        String userId = loginHelper.getCurrentAppUserId();

        QueryWrapper<BbsPostLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", id).eq("user_id", userId);

        // 判断是否已经点赞过
        boolean hasLiked = bbsPostLikeMapper.selectOne(queryWrapper) != null;

        // 点赞数更新部分，只更新 like_count 字段，避免全字段更新引发 JSON 字段异常
        UpdateWrapper<BbsPost> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);

        if (hasLiked) {
            // 取消点赞：点赞数减一，删除点赞记录
            // 先获取当前点赞数，防止负数
            Integer likeCount = baseMapper.selectById(id).getLikeCount();
            int newCount = (likeCount != null && likeCount > 0) ? likeCount - 1 : 0;
            updateWrapper.set("like_count", newCount);
            baseMapper.update(null, updateWrapper);

            bbsPostLikeMapper.delete(queryWrapper);
            return R.ok("取消点赞成功");
        } else {
            // 点赞：点赞数加一，新增点赞记录
            Integer likeCount = baseMapper.selectById(id).getLikeCount();
            int newCount = (likeCount != null) ? likeCount + 1 : 1;
            updateWrapper.set("like_count", newCount);
            baseMapper.update(null, updateWrapper);

            BbsPostLike like = new BbsPostLike();
            like.setPostId(id);
            like.setUserId(userId);
            bbsPostLikeMapper.insert(like);
            return R.ok("点赞成功");
        }
    }


}




