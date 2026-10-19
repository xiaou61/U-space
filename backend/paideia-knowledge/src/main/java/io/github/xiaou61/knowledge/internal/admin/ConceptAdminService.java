package io.github.xiaou61.knowledge.internal.admin;

import io.github.xiaou61.knowledge.internal.concept.Concept;
import io.github.xiaou61.knowledge.internal.concept.ConceptLinkMapper;
import io.github.xiaou61.knowledge.internal.concept.ConceptMapper;
import io.github.xiaou61.knowledge.internal.entry.EntryMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 概念的管理端写入用例（WORK-021）。
 *
 * <p>三处只能在这里判、数据库拦不住的：
 *
 * <ul>
 *   <li><b>名称全局唯一</b>——数据库有唯一键，但要在撞键之前给出可读的原因，且改名时
 *       要把自己排除在查重之外。</li>
 *   <li><b>边不能自环</b>——数据库有 CHECK，这里先给明确报错。</li>
 *   <li><b>两端必须存在</b>——外键会拦，但抛出的是数据库异常，这里换成 404。</li>
 * </ul>
 */
@Service
public class ConceptAdminService {

    private static final List<String> RELATION_TYPES = List.of("prerequisite", "related");
    private static final int MAX_NAME = 64;
    private static final int MAX_SUMMARY = 500;

    public record ConceptSummary(long id, String name, String summary, Long categoryId,
                                 long entryLinks, long edges) {
    }

    private final ConceptMapper conceptMapper;
    private final ConceptLinkMapper linkMapper;
    private final EntryMapper entryMapper;

    ConceptAdminService(ConceptMapper conceptMapper, ConceptLinkMapper linkMapper, EntryMapper entryMapper) {
        this.conceptMapper = conceptMapper;
        this.linkMapper = linkMapper;
        this.entryMapper = entryMapper;
    }

    public List<ConceptSummary> list() {
        return conceptMapper.findAll().stream()
                .map(concept -> new ConceptSummary(concept.getId(), concept.getName(), concept.getSummary(),
                        concept.getCategoryId(),
                        conceptMapper.countEntryLinks(concept.getId()),
                        conceptMapper.countEdges(concept.getId())))
                .toList();
    }

    @Transactional
    public Long create(Long categoryId, String name, String summary) {
        String trimmed = requireName(name);
        if (conceptMapper.countByName(trimmed, null) > 0) {
            throw conflict("已经有同名概念");
        }
        Instant now = Instant.now();
        Concept concept = new Concept();
        concept.setName(trimmed);
        concept.setSummary(normalizeSummary(summary));
        concept.setCategoryId(categoryId);
        concept.setCreatedAt(now);
        concept.setUpdatedAt(now);
        conceptMapper.insert(concept);
        return concept.getId();
    }

    @Transactional
    public void update(Long id, Long categoryId, String name, String summary) {
        Concept existing = requireConcept(id);
        String trimmed = requireName(name);
        if (conceptMapper.countByName(trimmed, id) > 0) {
            throw conflict("已经有同名概念");
        }
        existing.setName(trimmed);
        existing.setSummary(normalizeSummary(summary));
        existing.setCategoryId(categoryId);
        existing.setUpdatedAt(Instant.now());
        conceptMapper.update(existing);
    }

    /** 关联与边由外键级联删除；概念本身删掉即可。 */
    @Transactional
    public void delete(Long id) {
        requireConcept(id);
        conceptMapper.delete(id);
    }

    @Transactional
    public void linkEntry(Long conceptId, Long entryId) {
        requireConcept(conceptId);
        if (entryMapper.findByIdIncludingDraft(entryId).isEmpty()) {
            throw new BizException(ErrorCode.NOT_FOUND, "条目不存在");
        }
        linkMapper.linkEntry(entryId, conceptId, Instant.now());
    }

    /** 解除关联；本来没关联也成功（幂等）。 */
    @Transactional
    public void unlinkEntry(Long conceptId, Long entryId) {
        linkMapper.unlinkEntry(entryId, conceptId);
    }

    @Transactional
    public Long createEdge(Long fromConceptId, Long toConceptId, String relationType) {
        String type = normalizeType(relationType);
        if (fromConceptId.equals(toConceptId)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "概念不能指向自己");
        }
        requireConcept(fromConceptId);
        requireConcept(toConceptId);
        Instant now = Instant.now();
        linkMapper.insertEdge(fromConceptId, toConceptId, type, now);
        Long id = linkMapper.findEdgeId(fromConceptId, toConceptId, type);
        if (id == null) {
            // INSERT IGNORE 之后仍查不到只可能是并发删除，按内部错误处理而不是返回 null
            throw new BizException(ErrorCode.INTERNAL, "关系写入失败");
        }
        return id;
    }

    @Transactional
    public void deleteEdge(Long id) {
        linkMapper.deleteEdge(id);
    }

    private Concept requireConcept(Long id) {
        Concept concept = conceptMapper.findById(id);
        if (concept == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "概念不存在");
        }
        return concept;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "概念名称不能为空");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "概念名称不能超过 " + MAX_NAME + " 个字符");
        }
        return trimmed;
    }

    private static String normalizeSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return null;
        }
        String trimmed = summary.trim();
        if (trimmed.length() > MAX_SUMMARY) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "说明不能超过 " + MAX_SUMMARY + " 个字符");
        }
        return trimmed;
    }

    private static String normalizeType(String relationType) {
        if (!RELATION_TYPES.contains(relationType)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "关系类型只能是 prerequisite 或 related");
        }
        return relationType;
    }

    private static BizException conflict(String message) {
        return new BizException(ErrorCode.INVALID_ARGUMENT, message);
    }
}
