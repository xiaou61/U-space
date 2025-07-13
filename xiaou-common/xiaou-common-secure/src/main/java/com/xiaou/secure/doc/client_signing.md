# 客户端签名 & 加密

1. 生成时间戳 `timestamp`
2. 将业务参数与 `timestamp` 组装为 Map（不含 `sign`）
3. 对 Map 按参数名字典序排序，拼接成 `k1=v1&k2=v2` 形式
4. 使用服务器下发的 `sign-secret` 作为密钥，计算 HMAC-SHA256，得到 `sign`
5. 如果参数需要加密，将业务 JSON 使用 `aes-key` 进行 AES/CBC/PKCS5Padding 加密，放在 `data` 字段
6. 发送请求（支持 GET/POST），参数包括：`timestamp`、`sign`、`data`（可选）