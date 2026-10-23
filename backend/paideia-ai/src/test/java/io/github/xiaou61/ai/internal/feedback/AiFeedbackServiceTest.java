package io.github.xiaou61.ai.internal.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * 反馈提交的校验（WORK-036 C 组）。
 *
 * <p>钉住三件容易在"顺手放行"里丢掉的事：非法评价被拒、点踩必须给原因、点赞时原因被**丢弃**
 * 而不是让整个请求 400。
 */
class AiFeedbackServiceTest {

    private final AiFeedbackMapper mapper = mock(AiFeedbackMapper.class);
    private final AiFeedbackService service = new AiFeedbackService(mapper);

    @Test
    void rejectsUnknownRating() {
        assertThatThrownBy(() -> service.submit(1L, null, null, "t", "chat", "maybe", null))
                .isInstanceOf(BizException.class);
        verify(mapper, never()).upsert(any());
    }

    @Test
    void rejectsDownWithoutReason() {
        assertThatThrownBy(() -> service.submit(1L, null, null, "t", "chat", "down", null))
                .isInstanceOf(BizException.class);
        verify(mapper, never()).upsert(any());
    }

    @Test
    void rejectsReasonOutsideWhitelist() {
        // 白名单的意义：写错一个值应当在提交时就被拒，而不是等统计时发现多了一类
        assertThatThrownBy(() -> service.submit(1L, null, null, "t", "chat", "down", "太蠢了"))
                .isInstanceOf(BizException.class);
        verify(mapper, never()).upsert(any());
    }

    @Test
    void storesDownWithReason() {
        service.submit(7L, 3L, 11L, "abc", "chat", "down", "incorrect");

        ArgumentCaptor<AiFeedbackRow> saved = ArgumentCaptor.forClass(AiFeedbackRow.class);
        verify(mapper).upsert(saved.capture());
        assertThat(saved.getValue().getRating()).isEqualTo("down");
        assertThat(saved.getValue().getReason()).isEqualTo("incorrect");
        assertThat(saved.getValue().getTraceId()).isEqualTo("abc");
        assertThat(saved.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void upDiscardsReasonInsteadOfFailing() {
        // 先踩后赞时界面上"原因"可能还留着；因为一个不该存在的字段把点赞打成 400，
        // 是把实现细节漏给了用户。
        service.submit(7L, 3L, 11L, "abc", "chat", "up", "incorrect");

        ArgumentCaptor<AiFeedbackRow> saved = ArgumentCaptor.forClass(AiFeedbackRow.class);
        verify(mapper).upsert(saved.capture());
        assertThat(saved.getValue().getRating()).isEqualTo("up");
        assertThat(saved.getValue().getReason()).isNull();
    }

    @Test
    void blankTraceIdBecomesNull() {
        // 关闭采集时 done 事件里的 traceId 为 null；界面可能把它拼成空串传回来。
        // 存空串会让唯一键在 (user, '') 上撞车——所有"没挂轮次"的反馈会互相覆盖。
        service.submit(7L, null, null, "   ", "chat", "up", null);

        ArgumentCaptor<AiFeedbackRow> saved = ArgumentCaptor.forClass(AiFeedbackRow.class);
        verify(mapper).upsert(saved.capture());
        assertThat(saved.getValue().getTraceId()).isNull();
    }
}
