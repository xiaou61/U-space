package io.github.xiaou61.ai.internal.digest;

/**
 * 一个采集源（WORK-027）。
 *
 * <p><b>契约里的三条边界</b>（不是"加固可选项"）：
 * <ol>
 *   <li>只允许 {@code http}/{@code https}；</li>
 *   <li>不跟随到非预期主机的重定向；</li>
 *   <li>连接与读取都有超时。</li>
 * </ol>
 * 三条由 {@link DigestHttp} 统一做，实现类不各自重复。
 *
 * <p><b>失败语义：抛 {@link DigestSourceException}，由编排层单源 try/catch</b>。
 * 一个源坏了不该让整轮没有内容——那正是 AC-004 要求的。
 */
interface DigestSource {

    /** 源标识，如 {@code qbitai}。 */
    String key();

    /**
     * 抓这一轮的内容。
     *
     * @param sinceMillis 只取这个时刻之后发布的（epoch 毫秒）
     * @return 抓到的东西；**空列表是合法的**（该源今天没有新内容）
     */
    java.util.List<FetchedItem> fetchSince(long sinceMillis) throws DigestSourceException;

    /** 单源失败。带上是哪个源，便于日志点名。 */
    class DigestSourceException extends RuntimeException {
        DigestSourceException(String message) {
            super(message);
        }

        DigestSourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
