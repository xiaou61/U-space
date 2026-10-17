import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // happy-dom：本包含 .tsx（markdown 渲染与图片放大），需要 DOM 才能测
    environment: 'happy-dom',
  },
})
