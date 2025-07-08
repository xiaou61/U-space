package com.xiaou.bbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.entity.BbsPost;
import com.xiaou.bbs.domain.req.BbsPostReq;
import com.xiaou.bbs.domain.resp.BbsPostResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BbsPostService extends IService<BbsPost> {

    R<String> add(BbsPostReq req);

    R<String> delete(String id);

    R<String> deleteAdmin(String id);


    R<PageRespDto<BbsPostResp>> listPost(String categoryId, PageReqDto reqDto);

    R<PageRespDto<BbsPostResp>> listPostMy(PageReqDto reqDto);

    R<String> uploadImage(MultipartFile file);
}
