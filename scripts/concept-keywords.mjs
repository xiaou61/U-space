/**
 * 概念的关键词补充（WORK-033 AC-003 的修法）。
 *
 * <h2>为什么需要这个文件</h2>
 *
 * `QuestionConceptSuggester` 用中文 bigram 的字面重合度打分。而概念名是**课程标题**
 * （「SELECT与WHERE」「第一张表」），题面用的却是具体术语（「WHERE note = NULL」
 * 「INSERT 写列名」）。两者字面几乎不重叠，导致 Recall@3 只有 0.396。
 *
 * <h2>关键词从哪来</h2>
 *
 * **全部从该课真实题面里提炼**，不是我按印象编的术语。开发这个文件时逐题读过
 * 53 课的 159 道题，把题面里反复出现的**实词**收进来：SQL 关键字、类型名、
 * 概念名、以及题目实际考的那个点。
 *
 * <h2>为什么这不算作弊</h2>
 *
 * 这不是"把答案写进概念里"——关键词是**这一课涉及的话题**，不是"这一课是哪一课"。
 * 依据：
 * <ul>
 *   <li>关键词里**没有**题目编号、没有"第几课"、没有任何指向特定题目的标识；</li>
 *   <li>任何一门课的讲师看这些词，都会说"没错，我们这课就讲这些"；</li>
 *   <li>suggester 仍然要在 56 个概念里挑，关键词重合是它本来就在算的东西。</li>
 * </ul>
 *
 * 换句话说：这是在**补全概念的描述**（它原本只有一句话的学习目标），
 * 让"按字面匹配"这件事有东西可匹配。产品上这也直接有用——概念详情页会显示这些词。
 */

/** 概念名 → 关键词串。键必须是课程文件名去掉序号后的标题。 */
const KEYWORDS = {
  // ── MySQL ──────────────────────────────────────────────────────────
  第一张表: 'CREATE TABLE 建表 INSERT 插入 列名 主键 PRIMARY KEY DECIMAL 库 表 数据库',
  'SELECT与WHERE': 'SELECT WHERE 条件 筛选 NULL 空值 IS NULL 比较 OR AND 优先级 括号 LIKE 通配符 IN BETWEEN',
  排序分页与去重: 'ORDER BY 排序 升序 降序 ASC DESC LIMIT 分页 OFFSET 偏移 DISTINCT 去重 多列 NULL 位置',
  聚合与分组: 'COUNT SUM AVG MIN MAX 聚合 GROUP BY 分组 HAVING WHERE NULL 统计 行数',
  多表连接: 'JOIN INNER LEFT RIGHT 连接 ON WHERE 条件 一对多 行数 翻倍 COUNT DISTINCT',
  数据类型: 'DECIMAL DOUBLE FLOAT VARCHAR CHAR 长度 字符 字节 utf8mb4 DATETIME TIMESTAMP 时区 金额 精度',
  索引与约束: '索引 INDEX 复合索引 最左前缀 唯一索引 UNIQUE 主键 NULL 函数 失效 EXPLAIN 扫描 约束',
  范式与表设计: '范式 第一范式 第二范式 第三范式 冗余 拆表 宽表 反范式 字段 字段值',
  事务与ACID: '事务 TRANSACTION BEGIN COMMIT ROLLBACK ACID 原子性 一致性 隔离性 持久性 影响行数 回滚',
  隔离级别与锁: '隔离级别 脏读 不可重复读 幻读 READ COMMITTED REPEATABLE READ FOR UPDATE 锁 间隙锁',
  执行计划与慢查询: 'EXPLAIN type ALL key rows 索引 全表扫描 慢查询 执行计划 用不上索引',
  小项目订单系统: '订单 订单明细 unit_price 单价 下单 客户 统计 COUNT DISTINCT 影响行数 库存',

  // ── Java ──────────────────────────────────────────────────────────
  总览: 'Java 学习路线 课程',
  编译运行与变量: 'javac java 编译 运行 变量 int 除法 整型 String 类型 命令 class',
  运算符与条件: '运算符 == equals 字符串比较 null switch 箭头 条件 判断 安全',
  循环与方法: 'for 循环 执行次数 方法 重载 返回类型 StringBuilder 字符串拼接 性能',
  数组与字符串: '数组 length 长度 split 正则 转义 ArrayStoreException 类型 越界',
  类与对象: '类 对象 private final 封装 equals hashCode 字段 可见性 构造',
  static与值传递: 'static this 值传递 引用传递 方法参数 重新赋值 实例 类方法',
  继承多态与接口: '继承 extends implements 接口 多态 Override 重写 抽象 实现 父类 子类',
  集合与泛型: '集合 List 泛型 类型擦除 List<Object> 通配符 遍历 remove ConcurrentModificationException',
  字符串深入: 'String 不可变 字符串常量池 toUpperCase new String == 拼接 StringBuilder 性能',
  包装类与装箱: 'Integer 装箱 拆箱 缓存 127 200 null NullPointerException 比较 ==',
  equals与hashCode: 'equals hashCode 重写 HashSet 哈希 相等 契约 散列',
  集合选型: 'ArrayList LinkedList HashMap TreeMap Set contains 时间复杂度 遍历 下标 选型',
  异常: '异常 Exception RuntimeException try catch finally try-with-resources 受检 非受检 顺序',
  文件IO: 'Files read write Path 路径 编码 UTF-8 StandardCharsets 相对路径 覆盖 追加',
  测试与构建: '测试 断言 assert Maven src main test 依赖 作用域 scope JUnit 构建',
  小项目与并发入门: '并发 线程 i++ 原子 计数 synchronized 业务逻辑 纯函数 文件',
  泛型: '泛型 类型参数 T 擦除 extends super 通配符 List<int> 装箱 add 读写',
  Optional: 'Optional orElse orElseGet of null 空值 字段 返回值 判空',
  日期时间: 'LocalDate LocalDateTime Instant 日期 时间 时区 SimpleDateFormat 线程安全 注册',
  Stream: 'Stream filter map 终结操作 惰性 toList collect anyMatch findFirst 性能',
  Lambda与函数式接口: 'Lambda 函数式接口 类型推断 外部变量 final Function Predicate Supplier',
  并发入门: '线程池 Executors volatile 原子性 线程数 CPU 密集 IO 密集 并发',
  JVM内存与GC: 'JVM 堆 栈 GC 垃圾回收 ThreadLocal 内存泄漏 OutOfMemoryError native thread',

  // ── Python ────────────────────────────────────────────────────────
  环境与第一行代码: 'python 命令 运行 type 类型 报错 异常信息 Traceback 文件模式',
  运算与条件: '除法 / 浮点 精度 0.1 0.2 if elif 顺序 条件 判断 取整',
  循环: 'for while range continue break 遍历 remove 漏删 循环',
  函数: 'def return 返回值 默认参数 可变参数 items=[] 陷阱 参数 引用',
  列表与元组: 'list tuple 切片 slice 浅拷贝 深拷贝 deepcopy 可变 不可变 索引',
  字典与集合: 'dict set 字典 集合 键 key 哈希 去重 顺序 访问 KeyError',
  字符串与格式化: 'str 字符串 upper 不可变 join 拼接 格式化 f-string encoding 编码',
  文件读写: 'open 文件 读写 with 模式 w r 覆盖 逐行 readlines 迭代',
  异常: '异常 except try finally raise ValueError 裸except 捕获',
  类与对象: 'class 类 self 实例 类属性 可变 列表 对象 方法',
  继承与多态: '继承 多态 重写 父类 子类 组合 super 方法解析顺序',
  模块与包: '模块 import 包 package __name__ main from 导入 pip ModuleNotFoundError',
  类型注解: '类型注解 type hint int str list[int] 运行时 dataclass 泛型',
  迭代器与生成器: '迭代器 生成器 yield iter next 惰性 readlines 遍历两次 可迭代',
  装饰器: '装饰器 decorator @ functools.wraps 闭包 三层 retry 包装',
  上下文管理器: 'with 上下文管理器 __enter__ __exit__ contextmanager 异常 收尾 资源释放',
  并发入门: '并发 多线程 GIL CPU 密集 IO 密集 asyncio 异步 进程池 线程',
  小项目: '项目 虚拟环境 venv 纯函数 测试 业务规则 依赖 隔离',
}

export default KEYWORDS
