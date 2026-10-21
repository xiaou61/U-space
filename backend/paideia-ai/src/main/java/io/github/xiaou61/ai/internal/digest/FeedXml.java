package io.github.xiaou61.ai.internal.digest;

import java.io.StringReader;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * RSS 2.0 / Atom 的解析（WORK-027）。
 *
 * <p><b>四条 XXE 开关是必做项，不是加固可选项。</b> 外部站点给的 XML 和用户上传的 docx
 * 属于同一类风险面：{@code <!DOCTYPE ... <!ENTITY xxe SYSTEM "file:///etc/passwd">]>}
 * 能让解析器去读进程能读的任何文件。照抄 {@code ResumeTextExtractor} 的先例。
 *
 * <p><b>单个条目坏掉不整批失败</b>：RSS 的 {@code pubDate} 格式五花八门（RFC 822、
 * ISO 8601、甚至空串），一条坏时间不该让当天整份订阅没有内容。坏条目直接跳过。
 */
@Component
class FeedXml {

    /** 摘要线索截断长度。给模型看的，太长会白占上下文。 */
    private static final int HINT_LIMIT = 200;

    /**
     * 常见的时间格式，按出现频率排。
     *
     * <p>顺序有意义：先试 RFC 822/1123 的两种写法（RSS 主流），再试 ISO 的两种。
     * <ul>
     *   <li>**`+0800` 无冒号那种必须单列**——`DateTimeFormatter.RFC_1123_DATE_TIME` 只认
     *       `GMT` 或带冒号的偏移，不认它。</li>
     *   <li>**不带星期几的变体也要列**：真实源里星期几写错并不罕见，而带 `EEE` 的格式
     *       在严格模式下会因 "DayOfWeek differs" 直接解析失败（2026-10-21 实测踩到）。
     *       去掉星期几就没有这个校验，日期本身仍然准确。</li>
     * </ul>
     */
    private static final List<DateTimeFormatter> OFFSET_FORMATS = List.of(
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME);

    /** 没有星期几的变体：源把星期几写错时靠它们兜住。 */
    private static final List<DateTimeFormatter> WEEKDAY_FREE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm Z", java.util.Locale.ENGLISH));

    /** 解析结果：标题 / 链接 / 时间 / 摘要线索。 */
    record Entry(String title, String link, long publishedAt, String summaryHint) {
    }

    /**
     * 解析一份 RSS 或 Atom。
     *
     * @throws DigestSource.DigestSourceException 不是合法 XML，或带 DOCTYPE（拒绝）
     */
    List<Entry> parse(String xml, long fallbackMillis) {
        Document document;
        try {
            document = newBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (org.xml.sax.SAXException exception) {
            // disallow-doctype-decl 命中时也走这里——带 DOCTYPE 的文档被拒绝。
            throw new DigestSource.DigestSourceException("XML 解析被拒绝：" + exception.getMessage(),
                    exception);
        } catch (java.io.IOException exception) {
            throw new DigestSource.DigestSourceException("XML 读取失败", exception);
        }

        Element root = document.getDocumentElement();
        if (root == null) {
            return List.of();
        }
        String name = localName(root.getTagName());
        return switch (name) {
            case "rss" -> parseRss(root, fallbackMillis);
            case "feed" -> parseAtom(root, fallbackMillis);
            default -> List.of();
        };
    }

    private List<Entry> parseRss(Element root, long fallbackMillis) {
        NodeList items = root.getElementsByTagName("item");
        List<Entry> entries = new ArrayList<>();
        for (int index = 0; index < items.getLength(); index++) {
            Node node = items.item(index);
            if (!(node instanceof Element item)) {
                continue;
            }
            String title = textOf(item, "title");
            String link = textOf(item, "link");
            if (link.isEmpty()) {
                link = textOf(item, "guid");
            }
            String description = textOf(item, "description");
            long publishedAt = parseDate(textOf(item, "pubDate"), fallbackMillis);
            addIfUsable(entries, title, link, publishedAt, description);
        }
        return entries;
    }

    private List<Entry> parseAtom(Element root, long fallbackMillis) {
        NodeList entriesNodes = root.getElementsByTagName("entry");
        List<Entry> entries = new ArrayList<>();
        for (int index = 0; index < entriesNodes.getLength(); index++) {
            Node node = entriesNodes.item(index);
            if (!(node instanceof Element entry)) {
                continue;
            }
            String title = textOf(entry, "title");
            String link = "";
            NodeList links = entry.getElementsByTagName("link");
            for (int linkIndex = 0; linkIndex < links.getLength(); linkIndex++) {
                if (links.item(linkIndex) instanceof Element candidate) {
                    String rel = candidate.getAttribute("rel");
                    String href = candidate.getAttribute("href");
                    // rel 缺省是 alternate；优先取 alternate
                    if (!href.isEmpty() && (rel.isEmpty() || "alternate".equals(rel))) {
                        link = href;
                        break;
                    }
                }
            }
            String summary = textOf(entry, "summary");
            if (summary.isEmpty()) {
                summary = textOf(entry, "content");
            }
            String updated = textOf(entry, "published");
            if (updated.isEmpty()) {
                updated = textOf(entry, "updated");
            }
            addIfUsable(entries, title, link, parseDate(updated, fallbackMillis), summary);
        }
        return entries;
    }

    private static void addIfUsable(List<Entry> entries, String title, String link, long publishedAt,
                                    String summaryHint) {
        // 没有链接的条目无法去重也无法点开，直接丢
        if (link == null || link.isBlank()) {
            return;
        }
        entries.add(new Entry(title == null ? "" : title.strip(), link.strip(), publishedAt,
                truncate(summaryHint)));
    }

    /**
     * 解析时间。
     *
     * <p>先按 RFC 1123（RSS 常用），再试 ISO 8601（Atom 常用）。**都失败就用抓取时刻**——
     * 时间错一条比整批没有内容好。
     */
    private static long parseDate(String raw, long fallbackMillis) {
        if (raw == null || raw.isBlank()) {
            return fallbackMillis;
        }
        String value = raw.strip();
        // **RFC 822 的偏移写法 "+0800"（没有冒号）RFC_1123_DATE_TIME 不认**——
        // 2026-10-21 实测：整批 pubDate 都退回 fallback，等于时间全错。必须显式列出。
        for (DateTimeFormatter formatter : OFFSET_FORMATS) {
            try {
                return ZonedDateTime.parse(value, formatter).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // 换下一个格式
            }
            try {
                // 不带时区名的 +0800 需要 OffsetDateTime，ZonedDateTime 会要求 zone
                return java.time.OffsetDateTime.parse(value, formatter).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // 换下一个格式
            }
        }
        // 星期几写错的源：把开头的 "EEE, " 剥掉再试，日期本身仍然准确
        String withoutWeekday = value.replaceFirst("^[A-Za-z]{3},\\s*", "");
        if (!withoutWeekday.equals(value)) {
            for (DateTimeFormatter formatter : WEEKDAY_FREE_FORMATS) {
                try {
                    return java.time.OffsetDateTime.parse(withoutWeekday, formatter)
                            .toInstant().toEpochMilli();
                } catch (DateTimeParseException ignored) {
                    // 换下一个格式
                }
            }
        }
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // 落到本地时间格式
        }
        try {
            // 部分源给 "yyyy-MM-dd HH:mm:ss"（无时区，按 UTC 解释）
            return java.time.LocalDateTime
                    .parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException ignored) {
            return fallbackMillis;
        }
    }

    /** 去掉 HTML 标签并把空白压成一个空格。描述里常带 `<p>` 与 `&nbsp;`。 */
    private static String truncate(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String plain = raw.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replaceAll("\\s+", " ")
                .strip();
        return plain.length() <= HINT_LIMIT ? plain : plain.substring(0, HINT_LIMIT);
    }

    private static String textOf(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return "";
        }
        String value = nodes.item(0).getTextContent();
        return value == null ? "" : value;
    }

    private static String localName(String tagName) {
        int colon = tagName.indexOf(':');
        return colon < 0 ? tagName : tagName.substring(colon + 1);
    }

    /**
     * 建一个带四条 XXE 防护的 builder。
     *
     * <p>每条都是必要项：不设 {@code disallow-doctype-decl} 时，一份恶意 RSS 就能读本地文件。
     */
    private static DocumentBuilder newBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException exception) {
            throw new IllegalStateException("无法配置安全的 XML 解析器", exception);
        }
    }
}
