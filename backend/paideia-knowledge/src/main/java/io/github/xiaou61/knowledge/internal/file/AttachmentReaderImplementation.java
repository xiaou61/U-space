package io.github.xiaou61.knowledge.internal.file;

import io.github.xiaou61.knowledge.AttachmentReader;
import org.springframework.stereotype.Component;

/**
 * {@link AttachmentReader} 的实现（WORK-035 T-09）。
 *
 * <p>一行转发：真正干活的是 {@link FileStorage}（本地磁盘或对象存储，由配置决定）。
 * 这个类的全部价值是"把 internal 的实现暴露成包根的公开契约"——否则 sandbox 引不到它。
 */
@Component
class AttachmentReaderImplementation implements AttachmentReader {

    private final FileStorage storage;

    AttachmentReaderImplementation(FileStorage storage) {
        this.storage = storage;
    }

    @Override
    public java.util.Optional<byte[]> read(String fileId) {
        return storage.read(fileId);
    }

    @Override
    public String put(byte[] content, String contentType) {
        return storage.put(content, contentType);
    }
}
