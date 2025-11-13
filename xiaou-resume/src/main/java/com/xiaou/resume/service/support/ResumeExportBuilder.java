package com.xiaou.resume.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.resume.dto.ResumeExportRequest;
import com.xiaou.resume.dto.ResumePreviewResponse;
import com.xiaou.resume.dto.ResumeSectionDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 负责生成导出文件内容
 */
@Slf4j
@Component
public class ResumeExportBuilder {

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public ExportedFile buildFile(ResumePreviewResponse preview, ResumeExportRequest request) {
        String format = StrUtil.blankToDefault(request.getFormat(), "PDF").toUpperCase(Locale.ROOT);
        return switch (format) {
            case "PDF" -> new ExportedFile(buildFileName(preview, "pdf"), "application/pdf",
                    buildPdf(preview, request));
            case "WORD", "DOC", "DOCX" -> new ExportedFile(buildFileName(preview, "docx"),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    buildWord(preview, request));
            case "HTML" -> new ExportedFile(buildFileName(preview, "html"), "text/html;charset=UTF-8",
                    buildHtml(preview, request));
            default -> throw new BusinessException("暂不支持的导出格式: " + format);
        };
    }

    private byte[] buildPdf(ResumePreviewResponse preview, ResumeExportRequest request) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            BaseFont baseFont = BaseFont.createFont("STSong-Light", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font sectionFont = new Font(baseFont, 14, Font.BOLD);
            Font contentFont = new Font(baseFont, 12);
            Font metaFont = new Font(baseFont, 10, Font.ITALIC);

            document.add(new Paragraph(preview.getResume().getResumeName(), titleFont));

            if (StrUtil.isNotBlank(preview.getResume().getSummary())) {
                document.add(new Paragraph(preview.getResume().getSummary(), contentFont));
            }

            if (StrUtil.isNotBlank(request.getTheme())) {
                document.add(new Paragraph("主题：" + request.getTheme(), metaFont));
            }

            for (ResumeSectionDTO section : safeSections(preview)) {
                document.add(new Paragraph(section.getTitle(), sectionFont));
                document.add(new Paragraph(sectionContent(section), contentFont));
            }

            if (Boolean.TRUE.equals(request.getWatermark())) {
                document.add(new Paragraph("仅供 " + preview.getResume().getResumeName() + " 使用", metaFont));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("生成PDF失败", e);
            throw new BusinessException("生成PDF文件失败");
        }
    }

    private byte[] buildWord(ResumePreviewResponse preview, ResumeExportRequest request) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("SimSun");
            titleRun.setText(preview.getResume().getResumeName());

            if (StrUtil.isNotBlank(preview.getResume().getSummary())) {
                XWPFParagraph summaryParagraph = document.createParagraph();
                XWPFRun summaryRun = summaryParagraph.createRun();
                summaryRun.setFontFamily("SimSun");
                summaryRun.setFontSize(12);
                summaryRun.setText(preview.getResume().getSummary());
            }

            if (StrUtil.isNotBlank(request.getTheme())) {
                XWPFParagraph themeParagraph = document.createParagraph();
                XWPFRun themeRun = themeParagraph.createRun();
                themeRun.setFontFamily("SimSun");
                themeRun.setItalic(true);
                themeRun.setText("主题：" + request.getTheme());
            }

            for (ResumeSectionDTO section : safeSections(preview)) {
                XWPFParagraph sectionTitle = document.createParagraph();
                sectionTitle.setSpacingBefore(200);
                XWPFRun sectionRun = sectionTitle.createRun();
                sectionRun.setFontFamily("SimSun");
                sectionRun.setFontSize(14);
                sectionRun.setBold(true);
                sectionRun.setText(section.getTitle());

                XWPFParagraph sectionContent = document.createParagraph();
                XWPFRun contentRun = sectionContent.createRun();
                contentRun.setFontFamily("SimSun");
                contentRun.setFontSize(12);
                for (String line : sectionContent(section).split("\\r?\\n")) {
                    contentRun.setText(line);
                    contentRun.addBreak();
                }
            }

            if (Boolean.TRUE.equals(request.getWatermark())) {
                XWPFParagraph watermarkParagraph = document.createParagraph();
                watermarkParagraph.setAlignment(ParagraphAlignment.RIGHT);
                XWPFRun watermarkRun = watermarkParagraph.createRun();
                watermarkRun.setFontFamily("SimSun");
                watermarkRun.setFontSize(10);
                watermarkRun.setText("仅供 " + preview.getResume().getResumeName() + " 使用");
            }

            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("生成Word失败", e);
            throw new BusinessException("生成Word文件失败");
        }
    }

    private byte[] buildHtml(ResumePreviewResponse preview, ResumeExportRequest request) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"zh-cn\"><head><meta charset=\"UTF-8\"/>")
                .append("<title>").append(HtmlUtils.htmlEscape(preview.getResume().getResumeName())).append("</title>")
                .append("<style>")
                .append("body{font-family:'Microsoft YaHei',sans-serif;background:#f5f5f5;margin:0;padding:32px;}")
                .append(".card{max-width:820px;margin:0 auto;background:#fff;border-radius:12px;padding:32px;box-shadow:0 8px 30px rgba(0,0,0,0.08);}")
                .append(".summary{color:#555;margin-bottom:20px;white-space:pre-line;}")
                .append(".section{margin-bottom:24px;}")
                .append(".section-title{font-size:18px;font-weight:600;margin-bottom:8px;color:#222;}")
                .append(".section-content{color:#333;line-height:1.6;white-space:pre-line;}")
                .append(".watermark{margin-top:24px;font-size:13px;color:#999;text-align:right;}")
                .append("</style></head><body><div class=\"card\">")
                .append("<h1>").append(HtmlUtils.htmlEscape(preview.getResume().getResumeName())).append("</h1>");

        if (StrUtil.isNotBlank(preview.getResume().getSummary())) {
            html.append("<p class=\"summary\">")
                    .append(HtmlUtils.htmlEscape(preview.getResume().getSummary()))
                    .append("</p>");
        }

        if (StrUtil.isNotBlank(request.getTheme())) {
            html.append("<p>主题：").append(HtmlUtils.htmlEscape(request.getTheme())).append("</p>");
        }

        for (ResumeSectionDTO section : safeSections(preview)) {
            html.append("<div class=\"section\">")
                    .append("<div class=\"section-title\">").append(HtmlUtils.htmlEscape(section.getTitle())).append("</div>")
                    .append("<div class=\"section-content\">")
                    .append(HtmlUtils.htmlEscape(sectionContent(section)).replace("\n", "<br/>"))
                    .append("</div></div>");
        }

        if (Boolean.TRUE.equals(request.getWatermark())) {
            html.append("<div class=\"watermark\">仅供 ")
                    .append(HtmlUtils.htmlEscape(preview.getResume().getResumeName()))
                    .append(" 使用</div>");
        }

        html.append("</div></body></html>");
        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<ResumeSectionDTO> safeSections(ResumePreviewResponse preview) {
        return CollUtil.isEmpty(preview.getSections()) ? Collections.emptyList() : preview.getSections();
    }

    private String buildFileName(ResumePreviewResponse preview, String extension) {
        String base = StrUtil.blankToDefault(preview.getResume().getResumeName(), "resume");
        String sanitized = base.replaceAll("[^\\w\\u4e00-\\u9fa5-]", "-");
        String time = FILE_TIME_FORMATTER.format(LocalDateTime.now());
        return sanitized + "-" + preview.getResume().getVersion() + "-" + time + "." + extension;
    }

    private String sectionContent(ResumeSectionDTO section) {
        return StrUtil.blankToDefault(section.getContent(), "");
    }

    public record ExportedFile(String fileName, String contentType, byte[] content) {
    }
}
