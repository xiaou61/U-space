"""给现有 Java 课程补 chapter 与 line_summary（一次性脚本）。

WORK-024：关卡地图需要"章节分组"，所以每篇 frontmatter 要有 chapter。
"""
import glob
import io
import os

CHAPTERS = {
    '00': '入门',
    '01': '基础语法', '02': '基础语法', '03': '基础语法', '04': '基础语法',
    '05': '面向对象', '06': '面向对象', '07': '面向对象',
    '08': '进阶', '09': '进阶', '10': '进阶',
    '11': '工程实践', '12': '工程实践',
}
SUMMARY = '从装 JDK 到写出一个小项目：13 关，每关一篇长文 + 可运行示例 + 小测。'

for path in sorted(glob.glob('content/courses/java/*.md')):
    name = os.path.basename(path)
    number = name[:2]
    chapter = CHAPTERS.get(number)
    if chapter is None:
        print('skip', name)
        continue
    text = io.open(path, encoding='utf-8').read()
    lines = text.split('\n')
    if lines[0].strip() != '---':
        print('no frontmatter', name)
        continue
    end = lines.index('---', 1)
    fm = lines[1:end]
    if any(line.startswith('chapter:') for line in fm):
        print('already', name)
        continue
    out = []
    inserted = False
    for line in fm:
        out.append(line)
        if not inserted and line.startswith('order:'):
            out.append('chapter: ' + chapter)
            inserted = True
        if number == '00' and line.startswith('line_sort:'):
            out.append('line_summary: ' + SUMMARY)
    if not inserted:
        print('no order line', name)
        continue
    io.open(path, 'w', encoding='utf-8').write('\n'.join([lines[0]] + out + lines[end:]))
    print('ok', name, chapter)
