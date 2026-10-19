package io.github.xiaou61.knowledge.internal.concept;

/** 条目和概念的一条关联，以及概念之间的一条边。两者形状相同，读查询也一起走。 */
public final class ConceptLinks {

    private ConceptLinks() {
    }

    /** 条目 → 概念。 */
    public record EntryLink(long entryId, long conceptId) {
    }

    /** 概念 → 概念。*/
    public record Edge(long id, long fromConceptId, long toConceptId, String relationType) {
    }

}
