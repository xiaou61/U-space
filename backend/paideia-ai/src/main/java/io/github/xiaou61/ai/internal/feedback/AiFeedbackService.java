package io.github.xiaou61.ai.internal.feedback;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 助手回答的反馈（WORK-036 C 组）。
 *
 * <p><b>只收枚举，不收自由文本</b>：自由文本会变成一个需要审核、需要防注入、需要转义展示的
 * 内容系统，而这条链路要回答的问题只是"哪方面不好"——四个选项足够。
 *
 * <p><b>重复提交是覆盖</b>：同一用户对同一轮的第二次评价改写那一条（唯一键 + upsert），
 * 与 WORK-013 的重复抑制口径一致。`traceId` 为空时不做去重（没有轮次可挂）。
 */
@Service
public class AiFeedbackService {

    static final String RATING_UP = "up";
    static final String RATING_DOWN = "down";

    /** 踩的原因。**白名单而不是正则**：写错一个值应当在提交时就被拒，不是等统计时才发现多了一类。 */
    static final Set<String> REASONS = Set.of("off_topic", "too_verbose", "incorrect", "other");

    private static final int MAX_LIMIT_DAYS = 90;

    private final AiFeedbackMapper mapper;

    AiFeedbackService(AiFeedbackMapper mapper) {
        this.mapper = mapper;
    }

    /** 一条反馈的对外形状。字段名与表列同名，前端不必再映射一层。 */
    public record FeedbackView(long id, long userId, Long conversationId, Long messageId,
                        String traceId, String kind, String rating, String reason, Instant createdAt) {
    }

    /**
     * 提交一条反馈。
     *
     * @param rating up / down；其余一律 400
     * @param reason down 时必填且在 {@link #REASONS} 里；up 时被**丢弃**（不报错）
     */
    public void submit(long userId, Long conversationId, Long messageId, String traceId,
                       String kind, String rating, String reason) {
        if (!RATING_UP.equals(rating) && !RATING_DOWN.equals(rating)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "评价只能是 up 或 down");
        }
        String normalizedReason = null;
        if (RATING_DOWN.equals(rating)) {
            if (reason == null || !REASONS.contains(reason)) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT,
                        "点踩需要说明原因，可选：" + String.join(" / ", REASONS.stream().sorted().toList()));
            }
            normalizedReason = reason;
        }
        AiFeedbackRow row = new AiFeedbackRow();
        row.setUserId(userId);
        row.setConversationId(conversationId);
        row.setMessageId(messageId);
        row.setTraceId(traceId == null || traceId.isBlank() ? null : traceId.trim());
        row.setKind(kind);
        row.setRating(rating);
        // 点赞时 reason 被丢弃而不是拒绝：用户先踩后赞，界面上那个"原因"可能还留在状态里，
        // 因为一个不该存在的字段把点赞打成 400，是把实现细节漏给了用户。
        row.setReason(normalizedReason);
        row.setCreatedAt(Instant.now());
        mapper.upsert(row);
    }

    /** 管理端列表。时间窗缺省 7 天、上限 90 天——与调用统计同一口径。 */
    public PageResult<FeedbackView> page(Instant from, Instant to, String rating, String reason,
                                         String kind, PageQuery query) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(7, ChronoUnit.DAYS) : from;
        Instant floor = end.minus(MAX_LIMIT_DAYS, ChronoUnit.DAYS);
        Instant clamped = start.isBefore(floor) ? floor : start;
        String r = rating == null || rating.isBlank() ? null : rating;
        String why = reason == null || reason.isBlank() ? null : reason;
        String k = kind == null || kind.isBlank() ? null : kind;
        List<FeedbackView> items = mapper.page(clamped, end, r, why, k, query.limit(), query.offset())
                .stream()
                .map(AiFeedbackService::toView)
                .toList();
        return PageResult.of(mapper.count(clamped, end, r, why, k), query, items);
    }

    /** 概览用：好评数 / 差评数。 */
    public record RatingCounts(long up, long down) {
        /** 差评率；没有任何反馈时为 0（不是 NaN）。 */
        public double downRate() {
            long total = up + down;
            return total == 0 ? 0 : (double) down / total;
        }
    }

    public RatingCounts counts(Instant from, Instant to) {
        long up = 0;
        long down = 0;
        for (AiFeedbackMapper.CountBy row : mapper.countByRating(from, to)) {
            if (RATING_UP.equals(row.name())) {
                up = row.count();
            } else if (RATING_DOWN.equals(row.name())) {
                down = row.count();
            }
        }
        return new RatingCounts(up, down);
    }

    private static FeedbackView toView(AiFeedbackRow row) {
        return new FeedbackView(row.getId() == null ? 0 : row.getId(),
                row.getUserId() == null ? 0 : row.getUserId(),
                row.getConversationId(), row.getMessageId(),
                row.getTraceId(), row.getKind(), row.getRating(), row.getReason(), row.getCreatedAt());
    }
}
