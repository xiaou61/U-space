import { describe, expect, it } from 'vitest'

import { ApiError, createApiClient, type ApiEnvelope } from './index'

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}

function envelope<T>(overrides: Partial<ApiEnvelope<T>> = {}): ApiEnvelope<T> {
  return { code: 0, message: 'ok', data: null as T, traceId: 't-1', ...overrides }
}

describe('createApiClient', () => {
  it('code 为 0 时返回 data', async () => {
    const client = createApiClient({
      fetchImpl: async () => jsonResponse(200, envelope({ data: { status: 'UP' } })),
    })

    await expect(client.requestEnvelope<{ status: string }>('/api/v1/health')).resolves.toEqual({
      status: 'UP',
    })
  })

  it('业务错误码抛 ApiError 并保留错误码与追踪标识', async () => {
    const client = createApiClient({
      fetchImpl: async () =>
        jsonResponse(403, envelope({ code: 40300, message: '无权访问', traceId: 'trace-9' })),
    })

    const error = await client.requestEnvelope('/api/v1/me').catch((reason: unknown) => reason)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ code: 40300, message: '无权访问', traceId: 'trace-9' })
  })

  it('非统一结构的失败响应退回 HTTP 状态码', async () => {
    const client = createApiClient({
      fetchImpl: async () => new Response('boom', { status: 502 }),
    })

    const error = await client.requestEnvelope('/api/v1/me').catch((reason: unknown) => reason)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ code: 502 })
  })

  it('传入令牌时附带 Authorization 头，未传入时不带', async () => {
    const seen: Array<string | null> = []
    const client = createApiClient({
      getToken: () => 'token-abc',
      fetchImpl: async (_input, init) => {
        seen.push(new Headers(init?.headers).get('Authorization'))
        return jsonResponse(200, envelope())
      },
    })
    const anonymous = createApiClient({
      fetchImpl: async (_input, init) => {
        seen.push(new Headers(init?.headers).get('Authorization'))
        return jsonResponse(200, envelope())
      },
    })

    await client.requestEnvelope('/api/v1/me')
    await anonymous.requestEnvelope('/api/v1/me')

    expect(seen).toEqual(['Bearer token-abc', null])
  })

  it('request 用于非统一结构的端点，直接返回响应体', async () => {
    const client = createApiClient({
      fetchImpl: async () => jsonResponse(200, { status: 'UP', groups: ['liveness'] }),
    })

    await expect(client.request<{ status: string }>('/actuator/health')).resolves.toMatchObject({
      status: 'UP',
    })
  })

  it('成功状态但响应体不是合法 JSON 时抛 ApiError，而不是静默返回 undefined', async () => {
    const client = createApiClient({
      fetchImpl: async () => new Response('<html>', { status: 200 }),
    })

    const error = await client.request('/actuator/health').catch((reason: unknown) => reason)

    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).message).toContain('合法 JSON')
  })

  it('stream 用 text/event-stream 作为 Accept，并把未读完的响应交回调用方', async () => {
    let seenAccept: string | null = null
    const client = createApiClient({
      fetchImpl: async (_input, init) => {
        seenAccept = new Headers(init?.headers).get('Accept')
        return new Response('event:content\ndata:{"delta":"你好"}\n\n', {
          status: 200,
          headers: { 'content-type': 'text/event-stream' },
        })
      },
    })

    const response = await client.stream('/api/v1/ai/conversations/1/messages', { method: 'POST' })

    // 带 application/json 去要 text/event-stream 会被服务端 406；但**也不能只写 SSE**——
    // 开流前的校验失败要用 JSON 返回，只接受 SSE 会让那些错误退化成 500。
    expect(seenAccept).toBe('text/event-stream, application/json')
    expect(response.body).not.toBeNull()
    await expect(response.text()).resolves.toContain('data:{"delta":"你好"}')
  })

  it('stream 在非 2xx 时读完错误体并按统一结构抛 ApiError', async () => {
    const client = createApiClient({
      fetchImpl: async () =>
        jsonResponse(404, envelope({ code: 40400, message: '会话不存在或已过期' })),
    })

    const error = await client.stream('/api/v1/ai/conversations/9/messages', { method: 'POST' }).catch(
      (reason: unknown) => reason,
    )

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ code: 40400, message: '会话不存在或已过期' })
  })
})
