package io.github.xiaou61.knowledge.internal.entry;

import io.github.xiaou61.knowledge.internal.entry.Entry;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * 浏览记录写入（WORK-021）。
 *
 * <p>它记录的是"路过"，与 {@code learning_progress}（"开始学这一课"）是两件事：
 * 前者不该进课程进度统计，后者不能被一次无意点击触发。
 *
 * <p>失败不该影响阅读：写入异常在调用方吞掉（见 {@code EntryViewController}），
 * 这是旁路数据，不值得让阅读页报错。
 */
@Service
public class EntryViewService {

    private final EntryViewMapper mapper;
    private final EntryMapper entryMapper;

    EntryViewService(EntryViewMapper mapper, EntryMapper entryMapper) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
    }

    /**
     * 记一次浏览。
     *
     * <p>先校验条目仍在发布：对草稿上报成功等于告诉调用者"这里有个未发布的条目"，
     * 与自评、学习记录同一口径。
     */
    public void record(long userId, long entryId) {
        Entry entry = entryMapper.findPublishedById(entryId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "条目不存在"));
        mapper.upsert(userId, entry.getId(), Instant.now());
    }
}
