import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // happy-dom：本包含 .tsx（编辑器、结果表、执行计划树），需要 DOM 才能测
    environment: 'happy-dom',
  },
})
