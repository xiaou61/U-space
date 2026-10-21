import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // 主题初始值取自 <html> 的类、切换要写 document，所以需要 DOM 环境。
    // 用 happy-dom 而非 jsdom：environment 阶段实测快 12%–33%（2026-10-17）
    environment: 'happy-dom',
    include: ['src/**/*.test.{ts,tsx}'],
    setupFiles: ['./src/test-setup.ts'],
    /**
     * 超时统一放宽，**两个都要改**（2026-10-21 实测）：
     *
     * <p>`pnpm -r test` 会同时起 5 个 vitest 进程，CPU 抢满时这个包里的两个重活会踩线：
     * ① `markdown.test.tsx` 的 `beforeAll` 要现场 `import('streamdown')`（默认 10 秒）；
     * ② 语料用例 `headings-deep` 要回放 1658 个增量（默认 5 秒，实测争抢时 6 秒）。
     *
     * <p>后果不是"报一条错"，而是**整个失败被算成跳过**：连续跑三次，一次 154 全过、
     * 一次 1 条超时、一次 beforeAll 打穿导致 73 条静默不跑。所以宁可给足上限——
     * 它只是天花板，正常路径仍是秒级，不会让任何一次跑变慢。
     */
    testTimeout: 60_000,
    hookTimeout: 60_000,
  },
})
