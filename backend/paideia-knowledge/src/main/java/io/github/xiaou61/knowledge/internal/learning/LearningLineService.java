package io.github.xiaou61.knowledge.internal.learning;

import io.github.xiaou61.knowledge.internal.category.Category;
import io.github.xiaou61.knowledge.internal.category.CategoryMapper;
import io.github.xiaou61.knowledge.internal.entry.Entry;
import io.github.xiaou61.knowledge.internal.entry.EntryMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 学习线的投影（WORK-020）。
 *
 * <p><b>线不是一张表，是分类的一个标记</b>：分类上 `line_kind` 非空即为一条线，
 * 线名/slug/顺序都来自分类自己，站是它下面的已发布条目。这样"新增一条线"只需要建分类 + 写内容，
 * 不需要改代码、不需要迁移。
 *
 * <p>线的集合与站序是**服务端唯一真相**：前端不解析 `learning-path`、不按自己的规则排序。
 */
@Service
public class LearningLineService {

    private final CategoryMapper categoryMapper;
    private final EntryMapper entryMapper;
    private final LearningProgressService progressService;

    LearningLineService(CategoryMapper categoryMapper,
                        EntryMapper entryMapper,
                        LearningProgressService progressService) {
        this.categoryMapper = categoryMapper;
        this.entryMapper = entryMapper;
        this.progressService = progressService;
    }

    /** 一站：条目 id、标题、在本次返回里的序号（1 起）。 */
    public record Station(long entryId, String title, int index) {
    }

    /**
     * 一条线。
     *
     * @param kind         course / reading
     * @param stationCount 已发布条目数
     * @param passedCount  本人已通过的站数
     * @param nextEntry    本人第一个未通过的站；全部通过或没有站时为 null
     * @param dueReviewCount 本人到期该复习的站数
     */
    public record Line(
            long categoryId,
            String slug,
            String name,
            String kind,
            int sortOrder,
            int stationCount,
            int passedCount,
            Station nextEntry,
            int dueReviewCount) {
    }

    /**
     * 全部线，按分类的展示顺序。
     *
     * <p>**只返回有站且 line_kind 非空的分类**：一个空的线在界面上是一个点进去没东西的卡片。
     */
    public List<Line> list(long userId) {
        List<Category> categories = categoryMapper.findAll();
        if (categories.isEmpty()) {
            return List.of();
        }
        // 本人的记录按 entryId 索引；统计只需要状态与到期时间
        Map<Long, LearningProgressService.View> mine = new LinkedHashMap<>();
        for (LearningProgressService.View view : progressService.listMine(userId, 500)) {
            mine.put(view.entryId(), view);
        }
        Instant now = Instant.now();

        List<Line> lines = new ArrayList<>();
        for (Category category : categories) {
            String kind = category.getLineKind();
            if (kind == null || kind.isBlank()) {
                continue;
            }
            List<Entry> stations = entryMapper.findPublishedByCategoryOrdered(category.getId());
            if (stations.isEmpty()) {
                continue;
            }
            int passed = 0;
            int due = 0;
            int index = 0;
            Station next = null;
            for (Entry station : stations) {
                index++;
                LearningProgressService.View view = mine.get(station.getId());
                boolean stationPassed = view != null && LearningProgressService.STATUS_PASSED.equals(view.status());
                if (stationPassed) {
                    passed++;
                } else if (next == null) {
                    next = new Station(station.getId(), station.getTitle(), index);
                }
                if (view != null && view.reviewDueAt() != null && !view.reviewDueAt().isAfter(now)) {
                    due++;
                }
            }
            lines.add(new Line(category.getId(), category.getSlug(), category.getName(), kind,
                    category.getSortOrder(), stations.size(), passed, next, due));
        }
        lines.sort(Comparator.comparingInt(Line::sortOrder).thenComparingLong(Line::categoryId));
        return List.copyOf(lines);
    }
}
