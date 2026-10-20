"""重排 Java 课程编号与前置链（WORK-024）。

一次性脚本：把新增关卡插到正确位置，并把 order / prerequisite / title 一起改掉。

用法：python scripts/reorder-java-course.py
"""

import glob
import io
import os
import re

BASE = 'content/courses/java'

# 新顺序（文件名前缀决定顺序，order 与 title 里的编号跟着改）
ORDER = [
    ('00-总览.md', '入门'),
    ('01-编译运行与变量.md', '基础语法'),
    ('02-运算符与条件.md', '基础语法'),
    ('03-循环与方法.md', '基础语法'),
    ('04-数组与字符串.md', '基础语法'),
    ('05-类与对象.md', '面向对象'),
    ('06-static与值传递.md', '面向对象'),
    ('07-继承多态与接口.md', '面向对象'),
    ('08-集合与泛型.md', '进阶'),
    ('09-字符串深入.md', '进阶'),
    ('10-异常.md', '进阶'),
    ('11-文件IO.md', '进阶'),
    ('12-测试与构建.md', '工程实践'),
    ('13-小项目与并发入门.md', '工程实践'),
]


def parse(path):
    text = io.open(path, encoding='utf-8').read()
    lines = text.split('\n')
    end = lines.index('---', 1)
    return lines[1:end], lines[end:], text


def main():
    # 先把文件名归位（当前状态可能已被人手动动过）
    existing = {os.path.basename(p) for p in glob.glob(f'{BASE}/*.md')}
    for target, _ in ORDER:
        if target in existing:
            continue
        # 找"去掉编号后同名"的文件，改成目标名
        stem = re.sub(r'^\d+-', '', target)
        for name in list(existing):
            if re.sub(r'^\d+-', '', name) == stem:
                os.rename(f'{BASE}/{name}', f'{BASE}/{target}')
                existing.discard(name)
                existing.add(target)
                print('rename', name, '->', target)
                break

    titles = {}
    for index, (name, chapter) in enumerate(ORDER):
        path = f'{BASE}/{name}'
        if not os.path.exists(path):
            print('missing', name)
            continue
        fm, rest, _ = parse(path)
        lines = []
        for line in fm:
            if line.startswith('order:'):
                lines.append(f'order: {index}')
            elif line.startswith('chapter:'):
                lines.append(f'chapter: {chapter}')
            else:
                lines.append(line)
        # 前面几课没有 chapter 行，补上
        if not any(l.startswith('chapter:') for l in lines):
            pos = next(i for i, l in enumerate(lines) if l.startswith('order:'))
            lines.insert(pos + 1, f'chapter: {chapter}')
        io.open(path, 'w', encoding='utf-8').write('\n'.join(['---'] + lines + rest))
        titles[index] = next(l.split('title: ', 1)[1] for l in lines if l.startswith('title: '
                          .strip()) or l.startswith('title:'))

    # 第二遍：title 里的编号与 prerequisite 一起改（要等所有 title 都定下来）
    for index, (name, _) in enumerate(ORDER):
        path = f'{BASE}/{name}'
        fm, rest, _ = parse(path)
        new_title = None
        for line in fm:
            if line.startswith('title:'):
                raw = line.split('title:', 1)[1].strip()
                stripped = re.sub(r'^\d+\s*·\s*', '', raw)
                new_title = f'title: {index:02d} · {stripped}'
        lines = []
        for line in fm:
            if line.startswith('title:') and new_title:
                lines.append(new_title)
            elif line.startswith('prerequisite:'):
                if index == 0:
                    continue
                lines.append(f'prerequisite: {titles.get(index - 1, "").split("· ", 1)[-1]}')
            else:
                lines.append(line)
        io.open(path, 'w', encoding='utf-8').write('\n'.join(['---'] + lines + rest))
        print(f'{index:02d}', name)


main()
