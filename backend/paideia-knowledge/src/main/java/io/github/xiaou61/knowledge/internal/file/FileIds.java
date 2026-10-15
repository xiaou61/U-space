package io.github.xiaou61.knowledge.internal.file;

import java.util.UUID;

/**
 * 附件 id 的形状校验。
 *
 * <p><b>为什么两个实现必须共用它</b>：读端点免鉴权，"id 不可枚举"是唯一的安全前提。
 * 这个校验如果各写一份，将来换实现时极容易出现"一个实现校了、另一个忘了"——
 * 而那正是把全部附件变成可枚举的那一刻。所以它只有一处。
 */
final class FileIds {

    private FileIds() {
    }

    /** 只接受标准 UUID 文本形式（36 字符、可解析）。 */
    static boolean isUuid(String value) {
        if (value == null || value.length() != 36) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
