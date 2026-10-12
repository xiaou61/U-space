import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // jsdom：本包含 .tsx（悬浮壳与卡片），需要 DOM 才能测
    environment: 'jsdom',
  },
})
