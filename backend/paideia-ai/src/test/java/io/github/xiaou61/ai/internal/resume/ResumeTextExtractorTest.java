package io.github.xiaou61.ai.internal.resume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.platform.BizException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 抽取器的行为钉死在这里（WORK-018 TASK-002）。
 *
 * <p>夹具**在测试里用代码现造**而不是往仓库塞二进制：PDF 与 DOCX 都是"能被代码写出来"的格式，
 * 现造的好处是每个用例想验的那个点一眼可见（多少页、有没有文字层、有没有 DOCTYPE），
 * 而不是对着一个 30KB 的 .pdf 猜里面是什么。
 */
class ResumeTextExtractorTest {

    private final ResumeTextExtractor extractor = new ResumeTextExtractor();

    @Test
    @DisplayName("文本型 PDF：抽出正文，并按内容判断不是空文字层")
    void extractsTextPdf() throws IOException {
        byte[] pdf = pdf("Zhang San  Java backend  13800000000  zhangsan@example.com "
                + "Education Some University  Work Some Company  order system", 1);

        ResumeTextExtractor.ExtractedText result = extractor.extract(pdf, "cv.pdf");

        assertThat(result.text()).contains("Zhang San").contains("Java").contains("zhangsan@example.com");
        assertThat(result.pages()).isEqualTo(1);
        assertThat(result.truncated()).isFalse();
    }

    @Test
    @DisplayName("图片型 PDF（有页面但无文字层）：拒绝并说明是 OCR 问题，不是格式问题")
    void rejectsNoTextLayer() throws IOException {
        // 空白页 = 没有文字层的最小样本，正是扫描件的抽取结果
        byte[] pdf = pdf("", 1);

        assertThatThrownBy(() -> extractor.extract(pdf, "scan.pdf"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有可读的文字")
                .hasMessageContaining("OCR");
    }

    @Test
    @DisplayName("超过 20 页：只读前 20 页并标记截断，不静默丢内容")
    void truncatesBeyondPageLimit() throws IOException {
        byte[] pdf = pdf("Page one  Zhang San  Java  zhangsan@example.com  University  Company", 25);

        ResumeTextExtractor.ExtractedText result = extractor.extract(pdf, "long.pdf");

        assertThat(result.pages()).isEqualTo(25);
        assertThat(result.truncated()).isTrue();
        assertThat(result.text()).contains("Page one");
    }

    @Test
    @DisplayName("加密 PDF：要求提供无密码版本，不做空口令爆破")
    void rejectsEncrypted() throws IOException {
        byte[] pdf;
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.protect(new org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy(
                    "owner", "user", new org.apache.pdfbox.pdmodel.encryption.AccessPermission()));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            pdf = out.toByteArray();
        }

        assertThatThrownBy(() -> extractor.extract(pdf, "locked.pdf"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("密码");
    }

    @Test
    @DisplayName("文本型 DOCX：抽出段落文字")
    void extractsDocx() throws IOException {
        byte[] docx = docx("张三", "Java 后端开发", "某大学 计算机科学与技术", "某公司 负责订单系统 邮箱 zhangsan@example.com");

        ResumeTextExtractor.ExtractedText result = extractor.extract(docx, "cv.docx");

        assertThat(result.text()).contains("张三").contains("Java 后端开发").contains("某大学");
        assertThat(result.text()).contains("\n");
    }

    @Test
    @DisplayName("带 DOCTYPE 的 docx：被拒（XXE 防线），且不回显解析器原始报文")
    void rejectsDoctype() throws IOException {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                  <w:body><w:p><w:r><w:t>&xxe;</w:t></w:r></w:p></w:body>
                </w:document>
                """;
        byte[] docx = zip("word/document.xml", xml.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> extractor.extract(docx, "evil.docx"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("结构读不出来")
                .hasMessageNotContaining("/etc/passwd");
    }

    @Test
    @DisplayName("zip 但没有 word/document.xml：当作格式不对拒绝")
    void rejectsZipWithoutDocument() throws IOException {
        byte[] zip = zip("hello.txt", "not a docx".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> extractor.extract(zip, "x.docx"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("找不到正文");
    }

    @Test
    @DisplayName("旧版 .doc：明确要求另存为 docx 或 pdf")
    void rejectsLegacyDoc() {
        byte[] ole = new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};

        assertThatThrownBy(() -> extractor.extract(ole, "old.doc"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("另存为");
    }

    @Test
    @DisplayName("其它格式与非文件内容：拒绝且说清只支持 PDF 与 docx")
    void rejectsOtherFormats() {
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

        assertThatThrownBy(() -> extractor.extract(png, "cv.png"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只支持 PDF");
    }

    @Test
    @DisplayName("空文件：直接拒绝，不进入任何解析")
    void rejectsEmpty() {
        assertThatThrownBy(() -> extractor.extract(new byte[0], "cv.pdf"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("空的");
    }

    // ── 夹具 ────────────────────────────────────────────────────────

    /** 造一个有 {@code pages} 页、每页同一段文字的 PDF；{@code text} 为空则造无文字层的页。 */
    private static byte[] pdf(String text, int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int page = 0; page < pages; page++) {
                PDPage pdfPage = new PDPage();
                document.addPage(pdfPage);
                if (!text.isEmpty()) {
                    try (PDPageContentStream stream = new PDPageContentStream(document, pdfPage)) {
                        stream.beginText();
                        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        stream.newLineAtOffset(50, 700);
                        // PDFBox 的标准 14 字体写不了中文；这里只验"抽得出文字"，
                        // 中文识别的质量归 AC-012 的记录项，不在这条单测里假装验过。
                        stream.showText(ascii(text));
                        stream.endText();
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    /**
     * 把中文映射成等长的 ASCII 占位符。
     *
     * <p>标准 14 字体只支持 Latin-1。这里不引一条"嵌中文字体"的路径——那是渲染的事，
     * 而抽取器只需要证明"页面上的字能被读出来"。
     */
    private static String ascii(String text) {
        StringBuilder builder = new StringBuilder();
        for (char character : text.toCharArray()) {
            builder.append(character < 128 ? character : 'x');
        }
        return builder.toString();
    }

    /** 造一个最小可读的 docx：一个 w:document，每个入参一段。 */
    private static byte[] docx(String... paragraphs) throws IOException {
        StringBuilder body = new StringBuilder();
        for (String paragraph : paragraphs) {
            body.append("<w:p><w:r><w:t xml:space=\"preserve\">")
                    .append(escape(paragraph))
                    .append("</w:t></w:r></w:p>");
        }
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                  <w:body>%s</w:body>
                </w:document>
                """.formatted(body);
        return zip("word/document.xml", xml.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] zip(String entryName, byte[] content) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(content);
            zip.closeEntry();
        }
        return out.toByteArray();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
