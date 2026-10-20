"""给 Java 课程补 level（难度）标记（WORK-024）。

默认值按章节推（与 import-course.mjs 的 levelOfChapter 一致），
需要精调的课在 OVERRIDES 里单独指定。

用法：python scripts/add-course-levels.py
"""

import glob
import io
import os
import re

BASE = 'content/courses/java'

# 章节 → 默认难度
BY_CHAPTER = {
    '入门': 'beginner',
    '基础语法': 'beginner',
    '面向对象': 'intermediate',
    '集合与字符串': 'intermediate',
    '工程实践': 'intermediate',
    '泛型与函数式': 'advanced',
    '并发与运行时': 'advanced',
}

# 需要单独指定的（按"去掉编号后的名字"匹配）
OVERRIDES = {
    # 总览是导读，难度同入门
    '总览': 'beginner',
    # 集合选型偏实现细节，放 advanced
    '集合选型': 'advanced',
    # 字符串与包装类其实是新手最容易错的，仍算入门到进阶之间；保持 intermediate
}


def stem(filename):
    return re.sub(r'^\d+-', '', filename)[:-3]


def main():
    for path in sorted(glob.glob(f'{BASE}/*.md')):
        name = os.path.basename(path)
        text = io.open(path, encoding='utf-8').read()
        lines = text.split('\n')
        if lines[0].strip() != '---':
            print('no frontmatter', name)
            continue
        end = lines.index('---', 1)
        fm = lines[1:end]
        if any(line.startswith('level:') for line in fm):
            print('already', name)
            continue
        chapter = next((line.split(':', 1)[1].strip() for line in fm
                        if line.startswith('chapter:')), '')
        level = OVERRIDES.get(stem(name)) or BY_CHAPTER.get(chapter) or 'intermediate'
        # 插在 chapter 之后，紧挨着它才有可读性
        out = []
        inserted = False
        for line in fm:
            out.append(line)
            if not inserted and line.startswith('chapter:'):
                out.append(f'level: {level}')
                inserted = True
        if not inserted:
            out.append(f'level: {level}')
        io.open(path, 'w', encoding='utf-8').write('\n'.join([lines[0]] + out + lines[end:]))
        print(f'{name:34s} {level}')


main()
