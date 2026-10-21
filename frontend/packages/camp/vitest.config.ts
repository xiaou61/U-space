import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  // 本包含 .tsx（薄弱点与计划组件），与各 app 的 vitest 配置同一套插件：
  // 少了 react 插件 JSX 不认识，少了 tailwind 插件的 @import 解析不了 @paideia/ui 的样式入口
  plugins: [react(), tailwindcss()],
  test: {
    environment: 'happy-dom',
    // 本包多个用例查询同一个文案，必须让 @testing-library 的自动 cleanup 生效；
    // 它只在 afterEach 全局存在时注册，所以要开 globals
    globals: true,
  },
})
