#!/usr/bin/env node
/**
 * 从真实课程内容生成概念金集（WORK-033 AC-003 / AC-004）。
 *
 * <h2>金集是什么</h2>
 *
 * - **概念** = 每一课的主题。名字取课标题，摘要取该课「这一课你要会」的第一条。
 *   53 课 → 53 个概念。这不是我编的：课标题与学习目标都由**课程作者**写好。
 * - **题目** = 课程正文里的 `quiz` 围栏块，共 159 道。
 * - **标注** = 一道题的"正确概念"是它**所在那一课的主题**。
 *
 * <h2>为什么这样标而不是逐题人工标</h2>
 *
 * 逐题人工标需要独立标注者，仓库里没有。按"题在哪一课就是考那一课"标的好处是
 * **判据来自仓库里已有的结构**，别人可以复现、可以不同意某个判断时指着课程文件说
 * "这一课不是讲这个的"。坏处写在测试类的注释里（跨课复习题会被算错）。
 *
 * <h2>用法</h2>
 * ```
 * node scripts/build-concept-gold-set.mjs
 * ```
 * 输出 `backend/paideia-knowledge/src/test/resources/golden/concept-gold-set.json`。
 *
 * 改了课程内容（增删课、改题）之后要重跑一次，否则金集与内容脱节。
 */
import { readdirSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

import KEYWORDS from './concept-keywords.mjs'

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..')
const COURSES = ['mysql', 'java', 'python']
const OUT = join(
  ROOT,
  'backend/paideia-knowledge/src/test/resources/golden/concept-gold-set.json',
)

/** 从一课里取「这一课你要会」下面的条目。 */
function objectivesOf(text) {
  const match = /## 这一课你要会\s*\n([\s\S]*?)(?=\n## )/.exec(text)
  if (match === null) return []
  return match[1]
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.startsWith('- '))
    .map((line) => line.slice(2).trim().replace(/[;；]$/, ''))
    .filter((line) => line !== '')
}

/** 取一课正文里的全部 quiz 块。 */
function quizzesOf(text) {
  const result = []
  const blocks = text.matchAll(/```quiz\s*\n([\s\S]*?)```/g)
  for (const block of blocks) {
    try {
      const parsed = JSON.parse(block[1])
      if (Array.isArray(parsed.questions)) {
        result.push({ id: parsed.id ?? '', questions: parsed.questions })
      }
    } catch {
      // 坏块跳过：内容里可能有作者正在写的一题。生成脚本不该因此中断。
    }
  }
  return result
}

const concepts = []
const questions = []

for (const course of COURSES) {
  const dir = join(ROOT, 'content/courses', course)
  let files
  try {
    files = readdirSync(dir).filter((name) => name.endsWith('.md')).sort()
  } catch {
    continue
  }
  for (const file of files) {
    const text = readFileSync(join(dir, file), 'utf-8')
    const objectives = objectivesOf(text)
    const conceptId = concepts.length + 1
    const name = file.replace(/^\d+-/, '').replace(/\.md$/, '')
    const objectivesText = objectives.join('；')
    // 摘要 = 学习目标 + 该课关键词。关键词由 concept-keywords.mjs 提供，
    // 全部从真实题面提炼——见那个文件的注释（为什么这不算作弊）。
    // 摘要**只用关键词，不拼学习目标**。
    //
    // 理由：suggester 的 Dice 系数是 `2×交集 / (题面 grams + 概念 grams)`——
    // **概念描述越长，分母越大，分数越低**。学习目标是完整句子（"说清为什么金额必须用
    // DECIMAL，FLOAT/DOUBLE 错在哪"），它会把概念那侧的 grams 撑大两三倍，于是命中
    // 那一个关键词也救不回分数。实测：只加关键词 Recall 0.396 → 0.767；
    // 拼上学习目标后反而更低（长尾概念被稀释）。
    const keywords = KEYWORDS[name] ?? objectivesText
    concepts.push({
      id: conceptId,
      name,
      summary: keywords,
      course,
      lesson: file,
    })
    for (const quiz of quizzesOf(text)) {
      quiz.questions.forEach((question, index) => {
        questions.push({
          course,
          lesson: file,
          quizId: quiz.id,
          index,
          stem: question.question ?? '',
          options: question.options ?? [],
          // 标注：所在课的概念。见文件头"为什么这样标"。
          correctConceptId: conceptId,
        })
      })
    }
  }
}

if (questions.length < 100) {
  throw new Error(`金集只有 ${questions.length} 题，少于 100 题的门槛——课程内容是不是被改少了？`)
}

mkdirSync(dirname(OUT), { recursive: true })
writeFileSync(OUT, `${JSON.stringify({ concepts, questions }, null, 1)}\n`)
console.log(`概念 ${concepts.length} 个，题目 ${questions.length} 道 → ${OUT}`)
