"""重排 Java 课程编号、章节与前置链（WORK-024）。

**为什么要有这个脚本**：关卡顺序是"内容决策"（哪个知识点先讲），而文件名前缀、
frontmatter 的 order、title 里的编号、prerequisite 的课名四者必须同步改。
手动改四遍必然漏一处，漏了的表现是地图顺序错乱或前置指向不存在的课。

用法：
    1. 改下面的 ORDER（按真实讲课顺序列文件名，不带编号前缀也行）
    2. python scripts/reorder-java-course.py
"""

import glob
import io
import os
import re

BASE = 'content/courses/java'

# 讲课顺序。第二列是章节分组。
# 新增内容时插到正确位置即可，编号、title、prerequisite 全部自动重算。
ORDER = [
    ('总览', '入门'),
    ('编译运行与变量', '基础语法'),
    ('运算符与条件', '基础语法'),
    ('循环与方法', '基础语法'),
    ('数组与字符串', '基础语法'),
    ('类与对象', '面向对象'),
    ('static与值传递', '面向对象'),
    ('继承多态与接口', '面向对象'),
    ('集合与泛型', '进阶'),
    ('字符串深入', '进阶'),
    ('包装类与装箱', '进阶'),
    ('equals与hashCode', '进阶'),
    ('异常', '进阶'),
    ('文件IO', '进阶'),
    ('测试与构建', '工程实践'),
    ('小项目与并发入门', '工程实践'),
]


def strip_number(name):
    """去掉 `NN-` 前缀，留下说明部分（不含 .md）。"""
    return re.sub(r'^\d+-', '', name)[:-3]


def read_parts(path):
    text = io.open(path, encoding='utf-8').read()
    lines = text.split('\n')
    end = lines.index('---', 1)
    return lines[1:end], lines[end:], text


def main():
    # ── 1) 文件名归位：按 ORDER 顺序逐个找"去掉编号后同名"的文件，改成目标前缀 ──
    files = [os.path.basename(p) for p in glob.glob(f'{BASE}/*.md')]
    for index, (stem, _) in enumerate(ORDER):
        target = f'{index:02d}-{stem}.md'
        if target in files:
            continue
        match = next((f for f in files if strip_number(f) == stem), None)
        if match is None:
            print(f'! 找不到与「{stem}」对应的文件')
            continue
        os.rename(f'{BASE}/{match}', f'{BASE}/{target}')
        files.remove(match)
        files.append(target)

    # ── 2) 先把每课的标题主体算出来（不含编号），后面互相引用要用 ──
    body_titles = {}
    for index, (stem, _) in enumerate(ORDER):
        path = f'{BASE}/{index:02d}-{stem}.md'
        if not os.path.exists(path):
            continue
        fm, _, _ = read_parts(path)
        for line in fm:
            if line.startswith('title:'):
                raw = line.split('title:', 1)[1].strip()
                body_titles[index] = re.sub(r'^\d+\s*·\s*', '', raw)

    # ── 3) 改写 order / chapter / title / prerequisite ──
    for index, (stem, chapter) in enumerate(ORDER):
        path = f'{BASE}/{index:02d}-{stem}.md'
        if not os.path.exists(path):
            continue
        fm, rest, _ = read_parts(path)
        out = []
        seen = set()
        for line in fm:
            key = line.split(':', 1)[0]
            if key in ('order', 'chapter', 'prerequisite'):
                continue          # 稍后按位置重建
            if key == 'title':
                out.append(f'title: {index:02d} · {body_titles[index]}')
                out.append(f'order: {index}')
                out.append(f'chapter: {chapter}')
                if index > 0:
                    out.append(f'prerequisite: {body_titles[index - 1]}')
                seen.update(('title', 'order', 'chapter', 'prerequisite'))
                continue
            out.append(line)
        missing = {'title', 'order', 'chapter'} - seen
        if missing:
            print(f'! {stem} 缺少 {missing}')
            continue
        io.open(path, 'w', encoding='utf-8').write('\n'.join(['---'] + out + rest))
        print(f'{index:02d} {stem}（{chapter}）')


main()
