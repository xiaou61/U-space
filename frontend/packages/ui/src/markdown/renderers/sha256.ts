/**
 * SHA-256（纯 TypeScript 实现，WORK-032）。
 *
 * <p><b>为什么不用 `crypto.subtle`</b>：它只在**安全上下文**（https 或 localhost）可用。
 * 本项目明确不配 HTTPS（用户 2026-10-09 决定），线上是 `http://<ip>:8081`——于是
 * `crypto.subtle` 是 `undefined`，判题直接抛 `Cannot read properties of undefined`。
 * 这个缺陷本地测不出来（dev server 跑在 localhost 上，那是安全上下文），只有部署后才暴露。
 *
 * <p>换成纯实现后还有一个附带好处：浏览器、Node 测试、内容门禁三处算出的哈希**必然一致**，
 * 不再依赖运行环境提供的实现细节。
 *
 * <p>实现取自 FIPS 180-4 的标准流程，只支持字符串输入（按 UTF-8 编码）与十六进制输出——
 * 本项目的唯一用途是给规范序列化串算指纹，不需要流式与 HMAC。
 */

const K = new Uint32Array([
  0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
  0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
  0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
  0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
  0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
  0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
  0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
  0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
])

/** UTF-8 编码（不依赖 TextEncoder，避免再引入一个环境相关能力）。 */
function utf8(text: string): Uint8Array {
  const bytes: number[] = []
  for (const character of text) {
    const code = character.codePointAt(0) ?? 0
    if (code < 0x80) {
      bytes.push(code)
    } else if (code < 0x800) {
      bytes.push(0xc0 | (code >> 6), 0x80 | (code & 0x3f))
    } else if (code < 0x10000) {
      bytes.push(0xe0 | (code >> 12), 0x80 | ((code >> 6) & 0x3f), 0x80 | (code & 0x3f))
    } else {
      bytes.push(0xf0 | (code >> 18), 0x80 | ((code >> 12) & 0x3f),
        0x80 | ((code >> 6) & 0x3f), 0x80 | (code & 0x3f))
    }
  }
  return new Uint8Array(bytes)
}

function rotateRight(value: number, bits: number): number {
  return (value >>> bits) | (value << (32 - bits))
}

/** 算 SHA-256，返回小写十六进制。 */
export function sha256Hex(text: string): string {
  const message = utf8(text)
  const bitLength = message.length * 8

  // 填充：0x80 + 若干 0，直到长度 ≡ 56 (mod 64)，再接 64 位大端长度
  const paddedLength = (((message.length + 8) >> 6) + 1) << 6
  const padded = new Uint8Array(paddedLength)
  padded.set(message)
  padded[message.length] = 0x80
  // 长度高 32 位按本项目用量恒为 0（字符串不会超过 512MB），只写低 32 位
  padded[paddedLength - 4] = (bitLength >>> 24) & 0xff
  padded[paddedLength - 3] = (bitLength >>> 16) & 0xff
  padded[paddedLength - 2] = (bitLength >>> 8) & 0xff
  padded[paddedLength - 1] = bitLength & 0xff

  const hash = new Uint32Array([
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19,
  ])
  const w = new Uint32Array(64)

  for (let offset = 0; offset < paddedLength; offset += 64) {
    for (let index = 0; index < 16; index++) {
      const base = offset + index * 4
      w[index] = ((padded[base] ?? 0) << 24) | ((padded[base + 1] ?? 0) << 16)
        | ((padded[base + 2] ?? 0) << 8) | (padded[base + 3] ?? 0)
    }
    for (let index = 16; index < 64; index++) {
      const previous15 = w[index - 15] ?? 0
      const previous2 = w[index - 2] ?? 0
      const s0 = rotateRight(previous15, 7) ^ rotateRight(previous15, 18) ^ (previous15 >>> 3)
      const s1 = rotateRight(previous2, 17) ^ rotateRight(previous2, 19) ^ (previous2 >>> 10)
      w[index] = ((w[index - 16] ?? 0) + s0 + (w[index - 7] ?? 0) + s1) >>> 0
    }

    let [a, b, c, d, e, f, g, h] = [hash[0] ?? 0, hash[1] ?? 0, hash[2] ?? 0, hash[3] ?? 0,
      hash[4] ?? 0, hash[5] ?? 0, hash[6] ?? 0, hash[7] ?? 0]

    for (let index = 0; index < 64; index++) {
      const s1 = rotateRight(e, 6) ^ rotateRight(e, 11) ^ rotateRight(e, 25)
      const choice = (e & f) ^ (~e & g)
      const temp1 = (h + s1 + choice + (K[index] ?? 0) + (w[index] ?? 0)) >>> 0
      const s0 = rotateRight(a, 2) ^ rotateRight(a, 13) ^ rotateRight(a, 22)
      const majority = (a & b) ^ (a & c) ^ (b & c)
      const temp2 = (s0 + majority) >>> 0
      h = g
      g = f
      f = e
      e = (d + temp1) >>> 0
      d = c
      c = b
      b = a
      a = (temp1 + temp2) >>> 0
    }

    hash[0] = ((hash[0] ?? 0) + a) >>> 0
    hash[1] = ((hash[1] ?? 0) + b) >>> 0
    hash[2] = ((hash[2] ?? 0) + c) >>> 0
    hash[3] = ((hash[3] ?? 0) + d) >>> 0
    hash[4] = ((hash[4] ?? 0) + e) >>> 0
    hash[5] = ((hash[5] ?? 0) + f) >>> 0
    hash[6] = ((hash[6] ?? 0) + g) >>> 0
    hash[7] = ((hash[7] ?? 0) + h) >>> 0
  }

  return [...hash].map((word) => word.toString(16).padStart(8, '0')).join('')
}
