import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // happy-dom：本包含 .tsx（对比渲染），需要 DOM 才能测
    environment: 'happy-dom',
  },
})
