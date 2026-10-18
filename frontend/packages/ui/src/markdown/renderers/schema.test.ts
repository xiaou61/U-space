import { describe, expect, it } from 'vitest'

import {
  blankCount,
  judgeBug,
  judgeFill,
  judgeOrder,
  judgeRun,
  lineCount,
  parseCodeChallenge,
  parseCodeMap,
  parseCodeTour,
  parseLearningPath,
  parseQuiz,
  permalinkOf,
  scoreQuiz,
} from './schema'

/**
 * 解析与判定的边界。这里**不测"渲染出来好不好看"**——那是组件测试的事。
 * 重点是可预期的拒绝：作者写错时得到的是一条能定位到字段的错误，而不是白屏。
 */

const TOUR = {
  id: 'demo',
  repo: 'facebook/react',
  commit: 'd75b0697',
  license: 'MIT',
  file: 'packages/react-reconciler/src/ReactFiberWorkLoop.js',
  steps: [
    {
      title: '进入',
      line: 100,
      code: 'const root = prepare();\nworkLoop(root);',
      language: 'javascript',
      focus: [1],
      explain: '先准备再循环。',
    },
  ],
}

describe('parseCodeTour', () => {
  it('接受合法输入并算出行范围', () => {
    const result = parseCodeTour(JSON.stringify(TOUR))
    expect(result.ok).toBe(true)
    if (!result.ok) {
      return
    }
    expect(result.value.steps[0]?.line).toBe(100)
    expect(permalinkOf(result.value, result.value.steps[0]!)).toBe(
      'https://github.com/facebook/react/blob/d75b0697/packages/react-reconciler/src/ReactFiberWorkLoop.js#L100-L101',
    )
  })

  it('拒绝非 JSON 并给出可读原因', () => {
    const result = parseCodeTour('{oops')
    expect(result.ok).toBe(false)
    if (result.ok) {
      return
    }
    expect(result.error).toContain('JSON')
  })

  it('拒绝跟随分支的 commit（必须是固定小写十六进制）', () => {
    const result = parseCodeTour(JSON.stringify({ ...TOUR, commit: 'main' }))
    expect(result.ok).toBe(false)
    if (result.ok) {
      return
    }
    expect(result.error).toContain('commit')
  })

  it('拒绝越界的 focus（块内行号必须落在代码里）', () => {
    const broken = { ...TOUR, steps: [{ ...TOUR.steps[0], focus: [5] }] }
    const result = parseCodeTour(JSON.stringify(broken))
    expect(result.ok).toBe(false)
  })

  it('拒绝超长代码与空 steps', () => {
    expect(parseCodeTour(JSON.stringify({ ...TOUR, steps: [] })).ok).toBe(false)
    const long = { ...TOUR, steps: [{ ...TOUR.steps[0], code: 'x\n'.repeat(201) }] }
    expect(parseCodeTour(JSON.stringify(long)).ok).toBe(false)
  })

  it('拒绝 predict 的越界答案', () => {
    const broken = {
      ...TOUR,
      steps: [
        {
          ...TOUR.steps[0],
          predict: { question: '为什么？', options: ['A', 'B'], answer: 3, reveal: '因为 A' },
        },
      ],
    }
    expect(parseCodeTour(JSON.stringify(broken)).ok).toBe(false)
  })

  it('拒绝 file 里的路径穿越', () => {
    expect(parseCodeTour(JSON.stringify({ ...TOUR, file: '../etc/passwd' })).ok).toBe(false)
    expect(parseCodeTour(JSON.stringify({ ...TOUR, file: '/etc/passwd' })).ok).toBe(false)
  })
})

describe('parseCodeMap', () => {
  it('接受节点与边，并拒绝指向不存在节点的边', () => {
    const ok = parseCodeMap(
      JSON.stringify({
        nodes: [{ id: 'a', label: 'A', layer: 0, order: 0 }],
        edges: [],
      }),
    )
    expect(ok.ok).toBe(true)

    const broken = parseCodeMap(
      JSON.stringify({
        nodes: [{ id: 'a', label: 'A', layer: 0, order: 0 }],
        edges: [{ from: 'a', to: 'missing' }],
      }),
    )
    expect(broken.ok).toBe(false)
    if (broken.ok) {
      return
    }
    expect(broken.error).toContain('missing')
  })

  it('拒绝重复的节点 id', () => {
    const result = parseCodeMap(
      JSON.stringify({
        nodes: [
          { id: 'a', label: 'A', layer: 0, order: 0 },
          { id: 'a', label: 'B', layer: 0, order: 1 },
        ],
        edges: [],
      }),
    )
    expect(result.ok).toBe(false)
  })
})

describe('parseLearningPath', () => {
  it('entryId 可以缺省（该站不可点击）', () => {
    const result = parseLearningPath(JSON.stringify({ steps: [{ title: '总览' }] }))
    expect(result.ok).toBe(true)
    if (!result.ok) {
      return
    }
    expect(result.value.steps[0]?.entryId).toBeUndefined()
  })

  it('拒绝空路径', () => {
    expect(parseLearningPath(JSON.stringify({ steps: [] })).ok).toBe(false)
  })
})

describe('parseCodeChallenge', () => {
  it('填空的空格数必须与选项组一致', () => {
    const ok = parseCodeChallenge(
      JSON.stringify({
        id: 'f',
        kind: 'fill',
        title: '补全',
        template: 'a({{0}});\nb({{1}});',
        options: [['x', 'y'], ['z', 'w']],
        answers: [0, 0],
      }),
    )
    expect(ok.ok).toBe(true)

    const broken = parseCodeChallenge(
      JSON.stringify({
        id: 'f',
        kind: 'fill',
        title: '补全',
        template: 'a({{0}});',
        options: [['x'], ['y']],
        answers: [0, 0],
      }),
    )
    expect(broken.ok).toBe(false)
  })

  it('排序的 answer 必须是完整排列', () => {
    expect(
      parseCodeChallenge(JSON.stringify({ id: 'o', kind: 'order', title: '排序', lines: ['a', 'b'], answer: [1, 0] })).ok,
    ).toBe(true)
    expect(
      parseCodeChallenge(JSON.stringify({ id: 'o', kind: 'order', title: '排序', lines: ['a', 'b'], answer: [0, 1, 2] })).ok,
    ).toBe(false)
  })

  it('找 bug 的行号必须落在代码里', () => {
    expect(
      parseCodeChallenge(JSON.stringify({ id: 'b', kind: 'bug', title: '找 bug', code: 'a\nb', bugLine: 2 })).ok,
    ).toBe(true)
    expect(
      parseCodeChallenge(JSON.stringify({ id: 'b', kind: 'bug', title: '找 bug', code: 'a\nb', bugLine: 9 })).ok,
    ).toBe(false)
  })

  it('判定函数与题干一致', () => {
    const fill = { id: 'f', kind: 'fill' as const, title: 't', template: '{{0}}', options: [['a', 'b']], answers: [1] }
    expect(judgeFill(fill, [1])).toBe(true)
    expect(judgeFill(fill, [0])).toBe(false)
    expect(judgeFill(fill, [])).toBe(false)

    const order = { id: 'o', kind: 'order' as const, title: 't', lines: ['a', 'b'], answer: [1, 0] }
    expect(judgeOrder(order, [1, 0])).toBe(true)
    expect(judgeOrder(order, [0, 1])).toBe(false)

    const bug = { id: 'b', kind: 'bug' as const, title: 't', code: 'a\nb', bugLine: 2 }
    expect(judgeBug(bug, 2)).toBe(true)
    expect(judgeBug(bug, 1)).toBe(false)
  })

  it('未知题型被拒绝', () => {
    expect(parseCodeChallenge(JSON.stringify({ id: 'x', kind: 'run', title: 't' })).ok).toBe(false)
  })
})

describe('parseQuiz', () => {
  const quiz = {
    id: 'q1',
    questions: [
      { question: '1+1?', options: ['1', '2'], answer: 1, explain: '当然是 2' },
      { question: '天空?', options: ['蓝', '绿'], answer: 0 },
    ],
  }

  it('接受合法小测并算分', () => {
    const result = parseQuiz(JSON.stringify(quiz))
    expect(result.ok).toBe(true)
    if (!result.ok) {
      return
    }
    expect(scoreQuiz(result.value, [1, 0])).toEqual({ correct: 2, total: 2 })
    expect(scoreQuiz(result.value, [0, null])).toEqual({ correct: 0, total: 2 })
  })

  it('拒绝越界答案与超量题目', () => {
    expect(parseQuiz(JSON.stringify({ questions: [{ question: 'q', options: ['a'], answer: 0 }] })).ok).toBe(false)
    const many = { questions: Array.from({ length: 11 }, () => ({ question: 'q', options: ['a', 'b'], answer: 0 })) }
    expect(parseQuiz(JSON.stringify(many)).ok).toBe(false)
  })
})

describe('工具函数', () => {
  it('lineCount 与 blankCount', () => {
    expect(lineCount('a')).toBe(1)
    expect(lineCount('a\nb')).toBe(2)
    expect(blankCount('{{0}} + {{1}}')).toBe(2)
    expect(blankCount('no blanks')).toBe(0)
  })
})

describe('parseCodeChallenge · run（WORK-015）', () => {
  const RUN = {
    id: 'r1',
    kind: 'run',
    title: '打印',
    language: 'python',
    starter: 'print(1)',
    cases: [{ name: '样例', expected: '1' }],
  }

  it('接受合法运行题', () => {
    const result = parseCodeChallenge(JSON.stringify(RUN))
    expect(result.ok).toBe(true)
    if (!result.ok || result.value.kind !== 'run') {
      return
    }
    expect(result.value.cases[0]?.trim).toBe(true)
  })

  it('只允许 java / python', () => {
    expect(parseCodeChallenge(JSON.stringify({ ...RUN, language: 'ruby' })).ok).toBe(false)
  })

  it('至少要有一个用例', () => {
    expect(parseCodeChallenge(JSON.stringify({ ...RUN, cases: [] })).ok).toBe(false)
  })

  it('判定：输出相符且退出码 0 才算通过', () => {
    const testCase = { name: 't', expected: '42' }
    expect(judgeRun(testCase, { stdout: '42\n', exitCode: 0, timedOut: false, outOfMemory: false })).toBe(true)
    expect(judgeRun(testCase, { stdout: '42\n', exitCode: 1, timedOut: false, outOfMemory: false })).toBe(false)
    expect(judgeRun(testCase, { stdout: '42\n', exitCode: 0, timedOut: true, outOfMemory: false })).toBe(false)
    expect(judgeRun(testCase, { stdout: '43', exitCode: 0, timedOut: false, outOfMemory: false })).toBe(false)
  })

  it('trim:false 时逐字符比较', () => {
    const strict = { name: 't', expected: '42', trim: false }
    expect(judgeRun(strict, { stdout: '42\n', exitCode: 0, timedOut: false, outOfMemory: false })).toBe(false)
    expect(judgeRun(strict, { stdout: '42', exitCode: 0, timedOut: false, outOfMemory: false })).toBe(true)
  })
})
