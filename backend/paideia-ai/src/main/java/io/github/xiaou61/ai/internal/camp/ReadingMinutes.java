package io.github.xiaou61.ai.internal.camp;

/**
 * 单项任务的预计时长（WORK-029）。
 *
 * <p>只影响展示与装箱，**不参与排序与判定**——所以系数不准也只是"今天看起来排满了"，
 * 不会让判断出错。
 *
 * <p><b>400 字/分钟是未实测的系数</b>（中文技术文章带代码块，实际速度因人而异）。
 * 要改只改这一处常量，别在别处再写一个数。
 *
 * <p>纯函数。
 */
public final class ReadingMinutes {

    static final int CHARS_PER_MINUTE = 400;
    static final int READ_MIN = 5;
    static final int READ_MAX = 25;
    /** 含可交互块（quiz / challenge）的条目：读完还要动手，多给 10 分钟。 */
    static final int DO_EXTRA = 10;
    /** 没有载体内容的缺口项：给一个固定值，让它能进预算。 */
    static final int GAP = 15;
    /** 复习是回看，不需要重读全文。 */
    static final int REVIEW = 5;

    private ReadingMinutes() {
    }

    public static int of(int bodyChars, boolean hasBlocks) {
        int read = clampDivUp(Math.max(0, bodyChars));
        return hasBlocks ? read + DO_EXTRA : read;
    }

    private static int clampDivUp(int chars) {
        int minutes = (chars + CHARS_PER_MINUTE - 1) / CHARS_PER_MINUTE;
        return Math.max(READ_MIN, Math.min(READ_MAX, minutes));
    }
}
