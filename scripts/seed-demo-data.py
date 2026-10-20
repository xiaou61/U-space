"""在线上造一批"像真的"内容：知识概念 + 关系 + 自评 + 学习进度 + 笔记。

用法：
    PAIDEIA_BASE_URL=http://host:8081 PAIDEIA_ADMIN_USER=... PAIDEIA_ADMIN_PASSWORD=... \
        python scripts/seed-demo-data.py

为什么要有这个脚本：图谱页、我的自评、继续学习三块在空库里全是提示语，
既看不出功能也没法验收。概念必须挂在**真实条目**上，掌握度才是现算出来的真数据，
不是写死的假数字。

幂等：概念按 name upsert（表上有 uk），关系重复会 409 忽略，进度与自评按条目 upsert。
"""
import json
import os
import sys
import urllib.error
import urllib.request

BASE = os.environ.get('PAIDEIA_BASE_URL', 'http://127.0.0.1:8081')
USER = os.environ.get('PAIDEIA_ADMIN_USER')
PASSWORD = os.environ.get('PAIDEIA_ADMIN_PASSWORD')
if not USER or not PASSWORD:
    sys.exit('缺少 PAIDEIA_ADMIN_USER / PAIDEIA_ADMIN_PASSWORD')


def call(method, path, body=None, token=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header('Content-Type', 'application/json')
    if token:
        req.add_header('Authorization', 'Bearer ' + token)
    try:
        with urllib.request.urlopen(req, timeout=60) as response:
            raw = response.read().decode()
            return json.loads(raw)['data'] if raw else None
    except urllib.error.HTTPError as error:
        raw = error.read().decode()
        raise RuntimeError(f'{method} {path} → {error.code}: {raw[:300]}') from None


# 概念：(名字, 一句话, 挂在哪些条目标题上, 归属分类 id)
# 条目按**标题精确匹配**找 id——脚本里不写死数据库自增 id，换环境也能跑。
CONCEPTS = [
    ('变量与类型', '变量是给内存里的一个值起名字；类型决定这个值能做什么运算、占多大空间。',
     ['01 · 编译运行与变量', '01 · 环境与第一行代码'], 12),
    ('条件分支', '让程序按条件走不同路径；if / else 的每个分支都是一条可能的执行轨迹。',
     ['02 · 运算符与条件', '02 · 运算与条件'], 12),
    ('循环', '把重复的动作写成一次描述；关键是认清"什么时候停"，否则就是死循环。',
     ['03 · 循环与方法', '03 · 循环'], 12),
    ('函数', '把一段逻辑打包成一个有名字、可复用的单位；参数是输入，返回值是输出。',
     ['04 · 数组与字符串', '04 · 函数'], 12),
    ('数组与列表', '连续存放同类型元素的结构；按下标随机访问是 O(1)，中间插入要挪动。',
     ['04 · 数组与字符串', '05 · 列表与元组'], 12),
    ('类与对象', '类是模板，对象是按模板造出来的实例；对象把"数据"和"操作数据的方法"绑在一起。',
     ['05 · 类与对象', '10 · 类与对象'], 12),
    ('继承与多态', '子类复用父类的行为；多态是"同一句调用，不同对象做出不同反应"。',
     ['06 · static 与值传递', '11 · 继承与多态'], 12),
    ('异常处理', '把"正常路径"和"出错路径"分开写；catch 住的是你能处理的那一类，其余继续往上抛。',
     ['09 · 异常', '15 · 测试与构建'], 12),
    ('线程与并发', '线程是 CPU 调度的单位；并发快在重叠等待，慢在争抢同一份共享状态。',
     ['22 · 并发入门：线程、线程池与共享状态', '22 · 并发入门：线程、线程池与共享状态'], 12),
    ('垃圾回收', '回收"再也走不到"的对象；有 GC 不等于不会泄漏——只要还有引用指着，它就收不走。',
     ['23 · JVM 内存与垃圾回收入门', '23 · JVM 内存与垃圾回收入门'], 12),
    ('Lambda 与函数式接口', '把"一段行为"当参数传；函数式接口就是只有一个抽象方法的类型。',
     ['21 · Lambda 与函数式接口', '21 · Lambda 与函数式接口'], 12),
    ('Stream 流水线', '把"遍历→判断→收集"压成一条链；中间操作惰性，终结操作才真正跑。',
     ['20 · Stream：把循环压成一条链', '20 · Stream：把循环压成一条链'], 12),
    ('HTTP 请求的一生', '一次访问至少经过 DNS、TCP、TLS、HTTP 四段独立等待，慢在哪一段症状完全不同。',
     ['一次访问里到底发生了几件事'], 15),
    ('并发渲染', 'React 把一次渲染切成可中断的小块；被打断就丢弃重来，所以渲染必须是纯的。',
     ['React 的一次并发渲染是怎么开始的'], 16),
]

# 关系：(from, to, type)。prerequisite 表示 from 是 to 的前置。
EDGES = [
    ('变量与类型', '条件分支', 'prerequisite'),
    ('条件分支', '循环', 'prerequisite'),
    ('循环', '函数', 'prerequisite'),
    ('函数', '类与对象', 'prerequisite'),
    ('类与对象', '继承与多态', 'prerequisite'),
    ('函数', 'Lambda 与函数式接口', 'prerequisite'),
    ('Lambda 与函数式接口', 'Stream 流水线', 'prerequisite'),
    ('数组与列表', 'Stream 流水线', 'prerequisite'),
    ('类与对象', '垃圾回收', 'related'),
    ('线程与并发', '垃圾回收', 'related'),
    ('异常处理', '线程与并发', 'related'),
    ('HTTP 请求的一生', '并发渲染', 'related'),
]

# 自评：(条目标题, 等级)。等级见 SelfAssessmentLevel：understood / unsure
# 只用**已发布**条目——草稿学习者根本打不开，给它们打标记是自欺。
ASSESSMENTS = [
    ('04 · 函数', 'understood'),
    ('05 · 列表与元组', 'understood'),
    ('00 · Java 从零开始', 'understood'),
    ('09 · 字符串：不可变、池与拼接', 'understood'),
    ('10 · 包装类与自动装箱', 'understood'),
    ('11 · equals 与 hashCode 必须成对', 'understood'),
    ('12 · 集合选型：ArrayList、LinkedList、HashMap、TreeMap', 'understood'),
    ('一次访问里到底发生了几件事', 'understood'),
    ('06 · static 与值传递', 'unsure'),
    ('17 · 泛型：类型参数与通配符', 'unsure'),
    ('18 · Optional：该用在哪、不该用在哪', 'unsure'),
    ('22 · 并发入门：线程、线程池与共享状态', 'unsure'),
    ('23 · JVM 内存与垃圾回收入门', 'unsure'),
]

# 学习进度：(条目标题, 是否通关, 分数)。分数形状 {blockId: {score, total}}。
# 通关口径见 LearningProgressRules.passed：**challenges 与 quizzes 都非空且都及格**，
# 只给 quizzes 的话每一条都只会是 in_progress——那正是第一版脚本踩的坑。
PROGRESS = [
    ('01 · 环境与第一行代码', True, {'c1': {'score': 2, 'total': 2}}),
    ('04 · 函数', True, {'c1': {'score': 3, 'total': 3}}),
    ('05 · 列表与元组', True, {'c1': {'score': 2, 'total': 2}}),
    ('07 · 字符串与格式化', False, {'c1': {'score': 1, 'total': 3}}),
    ('00 · Java 从零开始', True, {'c1': {'score': 3, 'total': 3}}),
    ('06 · static 与值传递', True, {'c1': {'score': 2, 'total': 2}}),
    ('08 · 集合与泛型', True, {'c1': {'score': 3, 'total': 3}}),
    ('09 · 字符串：不可变、池与拼接', True, {'c1': {'score': 2, 'total': 2}}),
    ('10 · 包装类与自动装箱', True, {'c1': {'score': 3, 'total': 3}}),
    ('11 · equals 与 hashCode 必须成对', True, {'c1': {'score': 2, 'total': 2}}),
    ('12 · 集合选型：ArrayList、LinkedList、HashMap、TreeMap', False, {'c1': {'score': 1, 'total': 3}}),
    ('13 · 异常', True, {'c1': {'score': 3, 'total': 3}}),
    ('14 · 文件 I/O', False, {'c1': {'score': 2, 'total': 3}}),
    ('计算机基础 · 总览', True, {'c1': {'score': 3, 'total': 3}}),
    ('一次访问里到底发生了几件事', True, {'c1': {'score': 2, 'total': 2}}),
]

NOTES = [
    ('值传递还是引用传递', """Java 只有值传递。

- 基本类型传的是**值的副本**，改它不影响外面。
- 对象传的是**引用的副本**——副本指向同一个对象，所以能改对象内容，但改不了外面那个引用本身。

```java
void change(StringBuilder sb) { sb.append("x"); }  // 改内容，外面看得见
void reassign(StringBuilder sb) { sb = new StringBuilder("y"); }  // 换引用，外面看不见
```

记法：**能不能改到外面的引用，取决于你动的是引用还是它指的东西。**"""),
    ('为什么并发会变慢', """三个原因，从常见到少见：

1. **锁竞争**：抢同一把锁的线程在排队，多出来的线程只是在等。
2. **上下文切换**：线程数远超核数时，切换本身的开销吃掉并行收益。
3. **伪共享**：两个无关变量落在同一条缓存行上，互相把对方的缓存刷掉。

判断分界：**任务里有没有"等待"（IO、网络、锁）**。有等待，线程能重叠等待时间；纯计算，线程数超过核数只会更慢。"""),
    ('JVM 内存几块区域', """- **堆**：对象都在这，GC 的主战场。
- **虚拟机栈**：每个线程一份，方法调用压栈，局部变量在栈帧里。
- **方法区 / 元空间**：类元数据、常量池。
- **程序计数器**：当前执行到哪条指令。

`OutOfMemoryError: Java heap space` 是堆满了；`Metaspace` 是类加载太多；`unable to create native thread` 是栈/线程数到顶——三者调的参数完全不同。"""),
]


def entry_ids_by_title(token):
    """拉管理端的全量条目（**含草稿**），按标题索引。

    <p>不能用公开的 `/knowledge/entries`：它只返回已发布的，而轨道里前几关还是草稿，
    那些关卡挂不上概念、图谱就会缺前半段。
    """
    index = {}
    for page in range(1, 4):
        result = call('GET', f'/api/v1/knowledge/admin/entries?page={page}&size=100', token=token)
        for item in result['items']:
            index[item['title']] = item['id']
        if len(result['items']) < 100:
            break
    return index


def published_ids(token):
    """已发布条目的 id 集合——自评与学习进度只能打在已发布的条目上。"""
    ids = set()
    for page in range(1, 4):
        result = call('GET', f'/api/v1/knowledge/entries?page={page}&size=100', token=token)
        for item in result['items']:
            ids.add(item['id'])
        if len(result['items']) < 100:
            break
    return ids


def main():
    token = call('POST', '/api/v1/auth/login', {'identifier': USER, 'password': PASSWORD})['accessToken']
    entries = entry_ids_by_title(token)
    print(f'条目索引 {len(entries)} 条')

    # 1) 概念
    existing = {item['name']: item['id'] for item in call('GET', '/api/v1/knowledge/admin/concepts', token=token)}
    concept_ids = {}
    for name, summary, _, category_id in CONCEPTS:
        if name in existing:
            concept_ids[name] = existing[name]
            print(f'  = 概念 {name}')
            continue
        concept_ids[name] = call('POST', '/api/v1/knowledge/admin/concepts',
                                 {'name': name, 'summary': summary, 'categoryId': category_id},
                                 token=token)
        print(f'  + 概念 {name}')

    # 2) 条目挂概念
    linked = 0
    for name, _, titles, _ in CONCEPTS:
        for title in titles:
            entry_id = entries.get(title)
            if entry_id is None:
                print(f'  ! 找不到条目「{title}」，跳过 {name}')
                continue
            try:
                call('POST', f'/api/v1/knowledge/admin/concepts/{concept_ids[name]}/entries',
                     {'entryId': entry_id}, token=token)
                linked += 1
            except RuntimeError as error:
                print(f'  ! 关联失败 {name}→{title}: {error}')
    print(f'条目↔概念 {linked} 条')

    # 3) 关系
    made = 0
    for source, target, relation in EDGES:
        try:
            call('POST', '/api/v1/knowledge/admin/concept-edges',
                 {'fromConceptId': concept_ids[source], 'toConceptId': concept_ids[target],
                  'relationType': relation}, token=token)
            made += 1
        except RuntimeError as error:
            print(f'  ! 关系失败 {source}→{target}: {str(error)[:120]}')
    print(f'概念关系 {made} 条')

    # 4) 自评（只能打在已发布条目上）
    published = published_ids(token)
    done = 0
    for title, level in ASSESSMENTS:
        entry_id = entries.get(title)
        if entry_id is None or entry_id not in published:
            print(f'  ! 跳过自评「{title}」（不存在或未发布）')
            continue
        call('PUT', f'/api/v1/knowledge/entries/{entry_id}/self-assessment', {'level': level}, token=token)
        done += 1
    print(f'自评 {done} 条')

    # 5) 学习进度（同上）
    done = 0
    for title, passed, scores in PROGRESS:
        entry_id = entries.get(title)
        if entry_id is None or entry_id not in published:
            print(f'  ! 跳过进度「{title}」（不存在或未发布）')
            continue
        # 通过的条目两类分数都给（passed 要求两边都非空且都及格）；
        # 没通过的只给一类，落到 in_progress。
        body = {'quizzes': scores, 'challenges': scores} if passed else {'quizzes': scores}
        call('PUT', f'/api/v1/learning/entries/{entry_id}', body, token=token)
        done += 1
    print(f'学习进度 {done} 条')

    # 6) 笔记
    existing_notes = {item['title'] for item in call('GET', '/api/v1/notes', token=token)}
    done = 0
    for title, body in NOTES:
        if title in existing_notes:
            print(f'  = 笔记 {title}')
            continue
        call('POST', '/api/v1/notes', {'title': title, 'body': body}, token=token)
        done += 1
    print(f'笔记 {done} 条')

    print('\n完成。刷新学习者端首页 / 知识图谱 / 我的自评 三页即可看到。')


main()
