package io.github.xiaou61.knowledge.internal.category;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 分类的读取用例。
 *
 * <p>写用例（新建／改名／换父／删除）在 `admin` 侧单独一个服务——读路径与写路径的校验
 * 完全不同，混在一起会让"读"也被迫依赖管理员才有的东西。
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /** 扁平全量，按展示顺序。装树、取子树、取分类名都基于这一份。 */
    public List<Category> all() {
        return categoryMapper.findAll();
    }

    /**
     * 知识库浏览用的分类树。
     *
     * <p><b>排除学习轨道下的分类</b>（WORK-024）：`line_kind` 非空的分类属于「从零开始」，
     * 它的入口是 `/learn` 的关卡地图，不再是知识库。用户明确要求"从零开始不要是知识库"，
     * 把它留在分类树里会让同一个 Java 课程出现两个入口，且其中一个没有关卡与依赖关系。
     *
     * <p>管理端看的是另一条路径（`KnowledgeAdminController.categories`，不经过这里），
     * 所以管理员仍然能看到并维护这些分类。
     */
    public List<CategoryTree.Node> tree() {
        List<Category> browsable = categoryMapper.findAll().stream()
                .filter(category -> category.getLineKind() == null || category.getLineKind().isBlank())
                .toList();
        return CategoryTree.assemble(browsable);
    }

    /**
     * **全部分类**，包括学习轨道下的（WORK-024）。
     *
     * <p>管理端与导入脚本用它：它们要维护这些分类，所以不能看到被过滤后的树
     * （否则导入脚本会以为分类不存在，去建一个同名的，撞唯一键 409）。
     */
    public List<CategoryTree.Node> fullTree() {
        return CategoryTree.assemble(categoryMapper.findAll());
    }
}
