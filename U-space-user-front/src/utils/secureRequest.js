import http from './request'
import { aesEncrypt, sign as genSign } from './secure'

// securePost 重新实现：封装 { timestamp, data: cipher, sign }

export async function securePost (url, bizData = {}, { encrypt = true } = {}) {
  const timestamp = Math.floor(Date.now() / 1000) // 秒级时间戳，和后端配置一致

  // 若开启加密，将 bizData 加密为 Base64 字符串
  const cipherText = encrypt ? aesEncrypt(bizData) : JSON.stringify(bizData)

  // 组装待签名参数
  const payload = {
    timestamp,
    data: cipherText
  }

  // 生成签名
  payload.sign = genSign(payload)

  // 发送 JSON
  return http.post(url, payload, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 向后兼容：导出旧别名
export { securePost as securePostV2 } 