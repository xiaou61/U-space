package io.github.xiaou61.ai.internal.digest;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.internal.credential.CredentialService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 每日速递的编排（WORK-027）。
 *
 * <p>八个步骤，每步的失败语义都写死了：
 * <ol>
 *   <li>未启用 / 没有源配置 → 跳过并返回原因；</li>
 *   <li>逐源抓取，**单源失败只记日志**（AC-004）；</li>
 *   <li>合并 → 算指纹 → 批内去重；</li>
 *   <li>查库剔除已存在（只查今天±1 天）；</li>
 *   <li>按 {@code maxItemsPerRun} 截断（成本闸）；</li>
 *   <li>**一次** AI 调用：挑 + 写标题摘要；</li>
 *   <li>逐条入库；AI 没返回的标 {@code degraded}（AC-005）；</li>
 *   <li>返回统计。</li>
 * </ol>
 *
 * <p><b>空候选池必须不调 AI、不写行、不报错</b>（评审焦点第二条）——
 * 所有源都失败的那天，页面应该显示"今天没有内容"，而不是让定时任务炸出堆栈。
 *
 * <p><b>不扣用户积分</b>：与 WORK-025 同一取舍——抓取是平台行为，没有用户归属。
 */
@Service
public class DigestCollectionService {

    private static final Logger log = LoggerFactory.getLogger(DigestCollectionService.class);

    /** 前端分组固定顺序。 */
    static final List<String> GROUP_ORDER = List.of("news", "github", "community", "hackernews");

    private static final String STATE_READY = "ready";
    private static final String STATE_DEGRADED = "degraded";

    /** 处理中异常（重复键）按"已存在"处理，不算失败。 */
    private static final String DUPLICATE_KEY = "Duplicate";

    private final DigestProperties properties;
    private final RssDigestSource rssSource;
    private final GithubTrendingSource githubSource;
    private final HackerNewsSource hackerNewsSource;
    private final DigestSummarizer summarizer;
    private final DigestMapper mapper;
    private final CredentialService credentials;

    DigestCollectionService(DigestProperties properties, RssDigestSource rssSource,
                            GithubTrendingSource githubSource, HackerNewsSource hackerNewsSource,
                            DigestSummarizer summarizer, DigestMapper mapper,
                            CredentialService credentials) {
        this.properties = properties;
        this.rssSource = rssSource;
        this.githubSource = githubSource;
        this.hackerNewsSource = hackerNewsSource;
        this.summarizer = summarizer;
        this.mapper = mapper;
        this.credentials = credentials;
    }

    /** 一次运行的统计，供日志与测试断言。 */
    public record RunResult(int fetched, int duplicated, int created, int degraded, int sourcesFailed,
                            String skipReason) {

        static RunResult skipped(String reason) {
            return new RunResult(0, 0, 0, 0, 0, reason);
        }
    }

    /**
     * 跑一次完整流程。
     *
     * <p>**任何阶段失败都不抛给调用方**：定时任务里抛异常只会让下一轮更难排查。
     */
    public RunResult runOnce() {
        if (!properties.isEnabled()) {
            return RunResult.skipped("未启用");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long nowMillis = System.currentTimeMillis();
        long sinceMillis = nowMillis - Duration.ofHours(properties.getLookbackHours()).toMillis();

        // 1-2. 逐源抓取
        List<FetchedItem> fetched = new ArrayList<>();
        int sourcesFailed = 0;
        DigestHttp http = new DigestHttp(properties.getTimeoutSeconds());
        for (DigestProperties.RssSource source : properties.getRss()) {
            try {
                fetched.addAll(rssSource.fetch(source, http, sinceMillis, nowMillis));
            } catch (RuntimeException exception) {
                sourcesFailed++;
                log.warn("速递源 {} 抓取失败：{}", source.getKey(), exception.getMessage());
            }
        }
        try {
            fetched.addAll(githubSource.fetch(properties, http, nowMillis));
        } catch (RuntimeException exception) {
            sourcesFailed++;
            log.warn("GitHub 热榜抓取失败：{}", exception.getMessage());
        }
        try {
            fetched.addAll(hackerNewsSource.fetch(http, nowMillis));
        } catch (RuntimeException exception) {
            sourcesFailed++;
            log.warn("Hacker News 抓取失败：{}", exception.getMessage());
        }

        if (fetched.isEmpty()) {
            log.info("速递本次没有抓到任何内容（失败源 {} 个）", sourcesFailed);
            return new RunResult(0, 0, 0, 0, sourcesFailed, "本次没有抓到内容");
        }

        // 3. 批内去重（同一 URL 可能被两个源都抓到）
        Map<String, FetchedItem> byFingerprint = new LinkedHashMap<>();
        for (FetchedItem item : fetched) {
            String fingerprint = DigestFingerprint.of(item.url());
            byFingerprint.putIfAbsent(fingerprint, item);
        }

        // 4. 查库剔除（只查今天±1 天）
        List<String> candidates = new ArrayList<>(byFingerprint.keySet());
        Set<String> existing = new LinkedHashSet<>(
                mapper.existingFingerprints(today.minusDays(1), today.plusDays(1), candidates));
        List<String> fresh = candidates.stream().filter(fp -> !existing.contains(fp)).toList();
        if (fresh.isEmpty()) {
            log.info("速递本次候选 {} 条全部已存在", candidates.size());
            return new RunResult(fetched.size(), candidates.size(), 0, 0, sourcesFailed, "没有新内容");
        }

        // 5. 截断（成本闸）
        List<String> selected = fresh.size() > properties.getMaxItemsPerRun()
                ? fresh.subList(0, properties.getMaxItemsPerRun())
                : fresh;
        List<FetchedItem> items = selected.stream().map(byFingerprint::get).toList();

        // 6. 一次 AI 调用
        Optional<ModelAccess> access = credentials.access(0);
        Map<String, DigestSummarizer.Rewritten> rewritten = access.isEmpty()
                ? Map.of()
                : summarizer.summarize(items, selected, access.get(), properties.getAiTimeoutSeconds());
        if (access.isEmpty()) {
            log.warn("平台未配置 AI 凭据，速递全部按降级入库");
        }

        // 7. 逐条入库
        Instant now = Instant.now();
        int created = 0;
        int degraded = 0;
        for (int index = 0; index < items.size(); index++) {
            FetchedItem item = items.get(index);
            String fingerprint = selected.get(index);
            DigestSummarizer.Rewritten result = rewritten.get(fingerprint);
            boolean ready = result != null;
            if (!ready) {
                degraded++;
            }
            try {
                mapper.insert(today, item.groupKey(), item.sourceKey(), item.sourceName(),
                        ready && !result.title().isBlank() ? result.title() : item.title(),
                        ready ? result.summary() : null,
                        item.url(), fingerprint, item.heat(), item.meta(), item.publishedAt(),
                        ready ? STATE_READY : STATE_DEGRADED, now);
                created++;
            } catch (RuntimeException exception) {
                // 并发两个实例同时跑时，唯一约束会挡住后来者——按"已存在"处理，不记失败
                if (String.valueOf(exception.getMessage()).contains(DUPLICATE_KEY)) {
                    continue;
                }
                log.warn("速递入库失败（{}）：{}", item.url(), exception.getMessage());
            }
        }

        log.info("速递完成：候选 {} 条，已存在 {} 条，入库 {} 条（其中降级 {} 条），失败源 {} 个",
                fetched.size(), candidates.size() - fresh.size(), created, degraded, sourcesFailed);
        return new RunResult(fetched.size(), candidates.size() - fresh.size(), created, degraded,
                sourcesFailed, null);
    }

    /** 某天的条目，按 {@link #GROUP_ORDER} 分组。给读接口用。 */
    public Map<String, List<DigestMapper.Row>> groupedByDate(LocalDate date) {
        Map<String, List<DigestMapper.Row>> grouped = new LinkedHashMap<>();
        for (String group : GROUP_ORDER) {
            grouped.put(group, new ArrayList<>());
        }
        for (DigestMapper.Row row : mapper.findByDate(date)) {
            grouped.computeIfAbsent(row.groupKey(), key -> new ArrayList<>()).add(row);
        }
        return grouped;
    }
}
