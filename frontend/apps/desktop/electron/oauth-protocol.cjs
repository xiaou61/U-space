/**
 * `paideia://` 协议的解析（WORK-037）。
 *
 * <p>单独成文件而不是留在 `main.cjs` 里：`main.cjs` 一被 require 就会启动 Electron，
 * 而这段解析正是**唯一需要单元测试**的部分（它挡的是把任意字符串塞进 IPC）。
 */
const PROTOCOL = 'paideia'

/**
 * 从命令行参数里取出手握码。
 *
 * <p>只认 `paideia://oauth?handoff=...` 这一种形状，且握手码必须像 URL-safe base64：
 * 服务端会用同样的规则校验，这里多挡一层是避免把任意字符串塞进 IPC。
 *
 * <p>**它挡的不是"伪造登录"**——握手码一次性、60 秒有效，伪造也换不到令牌。
 * 挡的是"页面能把任意字符串送进主进程"这条通道。
 *
 * @returns 合法则返回握手码，否则 null
 */
function handoffFromUrl(url) {
  if (typeof url !== 'string' || !url.startsWith(`${PROTOCOL}://`)) {
    return null
  }
  let parsed
  try {
    parsed = new URL(url)
  } catch {
    return null
  }
  if (parsed.hostname !== 'oauth') {
    return null
  }
  const handoff = parsed.searchParams.get('handoff')
  if (handoff === null || handoff === '' || handoff.length > 128) {
    return null
  }
  return /^[A-Za-z0-9_-]+$/.test(handoff) ? handoff : null
}

module.exports = { PROTOCOL, handoffFromUrl }
