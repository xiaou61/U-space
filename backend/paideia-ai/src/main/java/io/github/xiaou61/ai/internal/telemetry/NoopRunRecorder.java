package io.github.xiaou61.ai.internal.telemetry;

import io.github.xiaou61.ai.RunOutcome;
import io.github.xiaou61.ai.RunRecorder;
import io.github.xiaou61.ai.RunSpec;
import io.github.xiaou61.ai.SpanSpec;

/**
 * 关掉采集时的实现（WORK-022 REQ-005）。
 *
 * <p>为什么要有它而不是到处判断 {@code enabled()}：埋点代码保持同一个形状
 * （`try (var run = recorder.begin(...))`），关掉时不改任何一个调用点。
 * 空实现必须是**真正的空操作**——不生成 id、不建对象图、不进队列。
 */
public final class NoopRunRecorder implements RunRecorder {

    private static final RunScope SCOPE = new NoopScope();
    private static final StepScope STEP = new NoopStep();

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public RunScope begin(RunSpec spec) {
        return SCOPE;
    }

    private static final class NoopScope implements RunScope {

        @Override
        public StepScope step(SpanSpec spec) {
            return STEP;
        }

        @Override
        public void attribute(String key, Object value) {
        }

        @Override
        public void tokens(int input, int output, int cacheRead) {
        }

        @Override
        public void markStarted() {
        }

        @Override
        public void markTtft() {
        }

        @Override
        public void addCredits(int charged) {
        }

        @Override
        public void finish(RunOutcome outcome, String errorType) {
        }

        @Override
        public void close() {
        }
    }

    private static final class NoopStep implements StepScope {

        @Override
        public void attribute(String key, Object value) {
        }

        @Override
        public void tokens(int input, int output, int cacheRead) {
        }

        @Override
        public void succeeded() {
        }

        @Override
        public void failed(String errorType) {
        }

        @Override
        public void close() {
        }
    }
}
