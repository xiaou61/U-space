package com.xiaou.auth.user.utils;

import com.aliyun.imageaudit20191230.models.ScanImageResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@Slf4j
public class AliGreenImageScanUtil {

    private String accessKeyId;
    private String accessKeySecret;
    private String regionId;

    // 场景值
    private static final List<String> SCENES = Arrays.asList("porn", "terrorism", "ad", "live", "logo");

    /**
     * 图片审核
     *
     * @param imageUrl 图片URL
     * @return 是否合规
     */
    public boolean scanImage(String imageUrl) {
        try {
            // 直接初始化 Client 对象
            com.aliyun.imageaudit20191230.Client client = new com.aliyun.imageaudit20191230.Client(
                    new Config()
                            .setAccessKeyId(this.accessKeyId)
                            .setAccessKeySecret(this.accessKeySecret)
                            .setRegionId(this.regionId)
            );

            // log.info("当前ak：{}", this.accessKeyId);
            // log.info("当前sk：{}", this.accessKeySecret);
            // log.info("当前region：{}", this.regionId);
            // log.info("图片URL：{}", imageUrl);

            // 设置图片任务
            com.aliyun.imageaudit20191230.models.ScanImageRequest.ScanImageRequestTask task0 = new com.aliyun.imageaudit20191230.models.ScanImageRequest.ScanImageRequestTask()
                    .setImageURL(imageUrl);

            // 创建审核请求
            com.aliyun.imageaudit20191230.models.ScanImageRequest scanImageRequest = new com.aliyun.imageaudit20191230.models.ScanImageRequest()
                    .setTask(Arrays.asList(task0))
                    .setScene(SCENES);

            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

            // 获取图片审核结果
            ScanImageResponse response = client.scanImageWithOptions(scanImageRequest, runtime);

            // 打印完整的响应数据（可选）
            log.debug("响应数据: {}", response.getBody());

            // 遍历所有场景，检查每个场景的审核建议
            for (String scene : SCENES) {
                String suggestion = response.getBody().getData().results.get(0).getSubResults().stream()
                        .filter(subResult -> scene.equals(subResult.getScene())) // 按场景匹配
                        .findFirst()
                        .map(subResult -> subResult.suggestion)
                        .orElse("pass"); // 默认值为 "pass"（如果找不到匹配的子结果）

                // 打印当前场景的审核结果
                log.info("场景: {}, 审核建议: {}", scene, suggestion);

                // 如果某个场景的建议是 "block"，则认为图片不合规
                if ("block".equals(suggestion)) {
                    log.warn("图片审核不合规，场景: {}", scene);
                    return false;  // 退出并返回 false
                }
            }

            // 如果没有任何场景被标记为 "block"，则认为合规
            return true;

        } catch (TeaException e) {
            log.error("阿里云SDK错误，错误码：{}，错误信息：{}", e.code, e.message);
        } catch (Exception e) {
            log.error("图片审核过程中出现未知错误：{}", e.getMessage());
        }
        return false;
    }
}