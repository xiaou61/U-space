/**
 * `handoffFromUrl` 的自检（WORK-037）。
 *
 * <p>桌面包没有单测运行器（它的测试全是 Playwright e2e），而这段解析挡的是
 * "把任意字符串送进主进程"这条通道，值得用最便宜的方式钉住：
 * `node electron/oauth-protocol.test.cjs`，退出码非 0 即失败。
 */
const assert = require('node:assert')
const { handoffFromUrl } = require('./oauth-protocol.cjs')

const GOOD = 'AbC-123_xyz'

// 正常形状
assert.strictEqual(handoffFromUrl(`paideia://oauth?handoff=${GOOD}`), GOOD)

// 协议名不对、host 不对、没有码 —— 一律 null
assert.strictEqual(handoffFromUrl('https://oauth?handoff=x'), null)
assert.strictEqual(handoffFromUrl('paideia://other?handoff=x'), null)
assert.strictEqual(handoffFromUrl('paideia://oauth'), null)
assert.strictEqual(handoffFromUrl('paideia://oauth?handoff='), null)

// 非法字符（URL 编码后的空格、斜杠、尖括号）必须被拒
assert.strictEqual(handoffFromUrl('paideia://oauth?handoff=a%20b'), null)
assert.strictEqual(handoffFromUrl('paideia://oauth?handoff=a/b'), null)

// 超长被拒：握手码是 43 字符，128 是宽松上限
assert.strictEqual(handoffFromUrl(`paideia://oauth?handoff=${'a'.repeat(129)}`), null)

// 非字符串输入不抛错
assert.strictEqual(handoffFromUrl(undefined), null)
assert.strictEqual(handoffFromUrl(null), null)
assert.strictEqual(handoffFromUrl(123), null)

// Windows 上 argv 里可能是带引号的整串
assert.strictEqual(handoffFromUrl(`"paideia://oauth?handoff=${GOOD}"`), null)

console.log('oauth-protocol: 全部断言通过')
