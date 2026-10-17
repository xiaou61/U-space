import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // 主题初始值取自 <html> 的类、切换要写 document，所以需要 DOM 环境。
    // 用 happy-dom 而非 jsdom：environment 阶段实测快 12%–33%（2026-10-17）
    environment: 'happy-dom',
    include: ['src/**/*.test.{ts,tsx}'],
    setupFiles: ['./src/test-setup.ts'],
  },
})
