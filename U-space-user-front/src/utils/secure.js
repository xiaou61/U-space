import CryptoJS from 'crypto-js'

// AES key & Sign secret 从环境变量注入，确保与后端 application.yml 保持一致
export const AES_KEY = import.meta.env.VITE_AES_KEY || 'xiaou-secure-123'
export const SIGN_SECRET = import.meta.env.VITE_SIGN_SECRET || 'xiaou-sign-secret'

// CBC 模式下 16 字节 iv，这里直接截取 key 前 16 位
const IV = CryptoJS.enc.Utf8.parse(AES_KEY.slice(0, 16))
const KEY = CryptoJS.enc.Utf8.parse(AES_KEY)

/**
 * AES/CBC/PKCS7Padding 加密
 * @param {string|Object} plaintext - 待加密明文，可传对象
 * @returns {string} Base64 ciphertext
 */
export function aesEncrypt (plaintext) {
  const text = typeof plaintext === 'string' ? plaintext : JSON.stringify(plaintext)
  const encrypted = CryptoJS.AES.encrypt(text, KEY, {
    iv: IV,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.ciphertext.toString(CryptoJS.enc.Base64)
}

/**
 * 生成 HMAC-SHA256 签名
 * @param {Record<string, any>} params - 字典序拼接字段
 * @returns {string}
 */
export function sign (params) {
  const sortedStr = Object.keys(params)
    .sort()
    .map(k => `${k}=${params[k]}`)
    .join('&')
  return CryptoJS.HmacSHA256(sortedStr, SIGN_SECRET).toString()
} 