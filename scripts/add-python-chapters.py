"""给 Python 课程补 chapter / level / line_summary（一次性脚本）。

WORK-024 的关卡地图需要"章节分组"与难度；Java 那批已经用 add-course-chapters.py
补过，Python 这批是在建表之前导入的，frontmatter 里没有这两项，于是轨道一直没登记上。
"""
import glob
import io
import os

CHAPTERS = {
    '00': '入门',
    '01': '基础语法', '02': '基础语法', '03': '基础语法', '04': '基础语法',
    '05': '容器', '06': '容器', '07': '容器',
    '08': '进阶', '09': '进阶',
    '10': '面向对象', '11': '面向对象',
    '12': '工程实践',
}
SUMMARY = '从装 Python 到写出一个小项目：13 关，每关一篇长文 + 可运行示例 + 小测。'

for path in sorted(glob.glob('content/courses/python/*.md')):
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
    if number == '00':
        out.append('line_summary: ' + SUMMARY)
    io.open(path, 'w', encoding='utf-8', newline='\n').write(
        '\n'.join(['---'] + out + ['---'] + lines[end + 1:])
    )
    print('ok', name, chapter)
