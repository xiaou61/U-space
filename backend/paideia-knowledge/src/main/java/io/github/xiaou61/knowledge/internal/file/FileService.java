package io.github.xiaou61.knowledge.internal.file;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件的上传与读取用例。
 *
 * <p><b>校验顺序是"先校验后落盘"</b>：类型、大小、魔数都在写文件之前完成，这样磁盘上
 * 不会留下半截或被拒的文件。落盘成功后才插表，插表失败则删掉刚写的文件。
 *
 * <p><b>三重校验缺一不可</b>：大小（防塞满磁盘）、魔数（防改名的伪文件）、扩展名与魔数
 * 是否自洽（防"内容是 png 却叫 .svg"这类边界混淆）。只信扩展名或只信客户端声明的
 * Content-Type 都等于没校验。
 */
@Service
public class FileService {

    private final FileStorage storage;
    private final StoredFileMapper storedFileMapper;
    private final KnowledgeStorageProperties properties;

    FileService(FileStorage storage, StoredFileMapper storedFileMapper, KnowledgeStorageProperties properties) {
        this.storage = storage;
        this.storedFileMapper = storedFileMapper;
        this.properties = properties;
    }

    /** 上传结果。{@code url} 是**稳定标识**而不是绝对地址：前端渲染时再补 baseUrl。 */
    public record UploadedFile(String id, String url, String contentType, long size, String originalName) {
    }

    public record FileContent(byte[] content, String contentType) {
    }

    public UploadedFile upload(MultipartFile upload, Long uploaderId) {
        if (upload == null || upload.isEmpty()) {
            throw invalid("附件不能为空");
        }
        if (upload.getSize() > properties.getMaxFileSize()) {
            throw invalid("附件超过上限 " + (properties.getMaxFileSize() / 1024 / 1024) + " MB");
        }

        byte[] content;
        try {
            content = upload.getBytes();
        } catch (IOException exception) {
            throw new BizException(ErrorCode.INTERNAL, "读取上传内容失败");
        }

        String sniffed = MediaTypeSniffer.sniff(content);
        if (sniffed == null) {
            throw invalid("不支持的附件类型；只允许 png、jpeg、gif、webp、pdf");
        }
        String originalName = sanitizeName(upload.getOriginalFilename());
        if (!MediaTypeSniffer.allowedExtensions(sniffed).contains(extensionOf(originalName))) {
            throw invalid("文件内容与扩展名不符（内容是 " + sniffed + "）");
        }

        String id = storage.put(content, sniffed);
        StoredFile file = new StoredFile();
        file.setId(id);
        file.setOriginalName(originalName);
        file.setContentType(sniffed);
        file.setSizeBytes(content.length);
        file.setUploaderId(uploaderId);
        try {
            storedFileMapper.insert(file);
        } catch (DataAccessException exception) {
            // 尽力回滚磁盘上的文件：否则会留下一条谁也引不到、也没人知道该删的孤儿
            storage.delete(id);
            throw new BizException(ErrorCode.INTERNAL, "保存附件记录失败");
        }

        return new UploadedFile(id, "/api/v1/knowledge/files/" + id, sniffed, content.length, originalName);
    }

    /**
     * 可直读时的跳转信息：URL + **这个 302 本身允许被缓存多久**。
     *
     * <p>两个值必须成对给出：签名 URL 每次都是新的，而浏览器缓存按完整 URL 索引——如果 302
     * 不允许缓存，本项之前靠"id 不变"换来的一年浏览器缓存就彻底没了（每次都回后端换签名）。
     */
    public record DirectRedirect(String url, Duration cacheFor) {
    }

    /**
     * 问后端实现：这个附件能不能直读（给出有签名的 URL）？
     *
     * <p><b>先查库再签名</b>：读端点免鉴权。若拿任意 id 直接去签名，这个端点就成了"任意 id 都能
     * 换到一个签名"的预言机；而"库里有没有这条记录"这件事本来就不该由它泄漏出去。
     * 库里没有 → 与读取一样给 404。
     *
     * <p>库里**有**记录但实现不支持直读（本地目录）→ 返回空，调用方照旧流字节。
     */
    public Optional<DirectRedirect> directRedirect(String id) {
        storedFileMapper.findById(id).orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "附件不存在"));
        return storage
                .presignedGetUrl(id, properties.getS3().getPresignTtl())
                .map(url -> new DirectRedirect(url, properties.getS3().getRedirectCache()));
    }

    public FileContent read(String id) {
        StoredFile file = storedFileMapper.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "附件不存在"));
        Optional<byte[]> content = storage.read(id);
        if (content.isEmpty()) {
            // 表里有记录但磁盘上没了（被运维清理、或换了实例）——按不存在处理并留痕，
            // 不要抛 500：这对调用方就是"这个附件没有了"
            throw new BizException(ErrorCode.NOT_FOUND, "附件文件缺失");
        }
        return new FileContent(content.get(), file.getContentType());
    }

    /** 只保留文件名本身：客户端可以传任意路径串进来，取末段避免出现目录痕迹。 */
    private static String sanitizeName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "attachment";
        }
        String name = raw.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? "attachment" : trimmed.substring(0, Math.min(trimmed.length(), 255));
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static BizException invalid(String message) {
        return new BizException(ErrorCode.INVALID_ARGUMENT, message);
    }
}
