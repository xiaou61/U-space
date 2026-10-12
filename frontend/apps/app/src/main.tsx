import { AssistantProvider, AssistantShell, createAssistantApi } from '@paideia/assistant'
import { AuthProvider, useAuth } from '@paideia/auth'
import { ThemeProvider } from '@paideia/ui'
import '@paideia/ui/styles.css'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode, useEffect } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'

import { auth } from './auth'
import { platform } from './host'
import { PlatformProvider } from './platform'
import { router } from './router'

/**
 * AI 精简的客户端。与页面用的 `api` 同一个装配来源（带令牌、401 刷新重放），
 * 因此它同样不认识令牌本身。
 */
const assistantApi = createAssistantApi(auth.api)

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 骨架期不做失败重试：连不上后端时希望立刻看到原因，而不是等几次重试
      retry: false,
      refetchOnWindowFocus: false,
    },
  },
})

/**
 * 把认证上下文的刷新实现注册给带令牌的 fetch。
 *
 * <p>必须在上下文内部做（只有这里拿得到 `refreshSession`），而数据客户端是要给页面直接 import
 * 的稳定单例，两者只能这样衔接。见 `./auth` 的说明。
 */
function RefreshBridge() {
  const { refreshSession } = useAuth()
  useEffect(() => {
    auth.registerRefresh(refreshSession)
  }, [refreshSession])
  return null
}

const container = document.getElementById('root')
if (container === null) {
  throw new Error('找不到挂载节点 #root')
}

createRoot(container).render(
  <StrictMode>
    <PlatformProvider platform={platform}>
      <ThemeProvider storage={platform.cache}>
        <QueryClientProvider client={queryClient}>
          <AuthProvider tokens={auth.tokens} api={auth.authApi}>
            <RefreshBridge />
            {/* 悬浮助手壳挂在路由之外：它要**贯穿全站**常驻，而不是每个页面自己摆一个 */}
            <AssistantProvider>
              <RouterProvider router={router} />
              <AssistantShell api={assistantApi} />
            </AssistantProvider>
          </AuthProvider>
        </QueryClientProvider>
      </ThemeProvider>
    </PlatformProvider>
  </StrictMode>,
)
