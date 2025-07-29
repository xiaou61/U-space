# xiaou-common-secure

基于注解 `@SecureApi` 的接口安全组件，为 **核心接口** 提供：

1. **参数签名**：HMAC-SHA256，防篡改、鉴别请求合法性
2. **时间戳校验**：默认 5 分钟有效期，抵御重放攻击
3. **AES 加密**：支持对业务数据做 AES/CBC/PKCS5Padding 加密，防窃听

> ⚡ 上线后可实现异常请求（签名错、超时、伪造等）100 % 拦截。

---
## 目录
1. [后端接入](#后端接入)
2. [前端接入](#前端接入)
3. [常见错误排查](#常见错误排查)
4. [高级用法](#高级用法)

---
## 后端接入
### 1. 引入依赖
```xml
<dependency>
    <groupId>com.xiaou</groupId>
    <artifactId>xiaou-common-secure</artifactId>
</dependency>
```
> Spring Boot 会自动装配 `SecureAutoConfiguration`，无须额外配置。

### 2. 添加注解
```java
@RestController
@RequestMapping("/api")
public class DemoController {

    @SecureApi                // 👉 生效！
    @PostMapping("/studentEntity/save")
    public R<Void> saveStudent(HttpServletRequest request) {
        String json = (String) request.getAttribute("secureData"); // 解密后明文
        StudentDTO dto = JSON.parseObject(json, StudentDTO.class);
        return R.ok();
    }
}
```

### 3. application.yml
```yaml
secure:
  aes-key: xiaou-secure-123         # 16/24/32 字符
  sign-secret: xiaou-sign-secret    # 用于 HMAC-SHA256
  allowed-timestamp-offset: 300     # 允许客户端与服务器时间差，单位秒
```

---
## 前端接入
以 **Vue + axios** 为例，其他框架同理。

### 1. 安装依赖
```bash
npm i crypto-js
```

### 2. 编写工具 (src/utils/secure.js)
```javascript
import CryptoJS from 'crypto-js';

const AES_KEY  = import.meta.env.VITE_AES_KEY;      // 16/24/32 字符，与后端保持一致
const SIGN_KEY = import.meta.env.VITE_SIGN_SECRET;  // 与后端 sign-secret 一致

// AES/CBC/PKCS5Padding 加密 → Base64
export function aesEncrypt(plainText) {
  const key = CryptoJS.enc.Utf8.parse(AES_KEY);
  const iv  = CryptoJS.enc.Utf8.parse(AES_KEY.slice(0, 16));
  const encrypted = CryptoJS.AES.encrypt(plainText, key, {
    iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  });
  return encrypted.ciphertext.toString(CryptoJS.enc.Base64);
}

// 生成签名：字典序拼接后做 HMAC-SHA256
export function sign(params) {
  const sortedStr = Object.keys(params)
    .sort()
    .map(k => `${k}=${params[k]}`)
    .join('&');
  return CryptoJS.HmacSHA256(sortedStr, SIGN_KEY).toString();
}
```

### 3. axios 请求拦截器 (src/utils/request.js)
```javascript
import axios from 'axios';
import { aesEncrypt, sign } from './secure';

const service = axios.create({ baseURL: '/api', timeout: 10_000 });

service.interceptors.request.use(config => {
  const timestamp = Math.floor(Date.now() / 1000);
  const bizData   = config.method === 'get' ? (config.params || {}) : (config.data || {});

  // 可根据需要决定是否加密 bizData
  const encryptedData = aesEncrypt(JSON.stringify(bizData));

  // 待签名参数
  const secureParams = { timestamp, data: encryptedData };
  secureParams.sign  = sign(secureParams);

  // 放置到请求
  if (config.method === 'get') {
    config.params = secureParams;
  } else {
    config.data   = secureParams;
    config.headers['Content-Type'] = 'application/json';
  }
  return config;
});

export default service;
```

### 4. 示例调用
```javascript
import request from '@/utils/request';

export function saveStudent(dto) {
  return request.post('/studentEntity/save', dto);
}
```
> 后端在切面里自动解密并注入到 `request.getAttribute("secureData")`。

### 5. GET / 表单请求
- **GET**：所有字段放在 query string，拦截器已自动处理。
- **表单**：将 `timestamp / data / sign` 作为普通表单字段提交即可。

---
## 常见错误排查
| 后端异常                     | 原因                                    | 解决方法                             |
|-----------------------------|-----------------------------------------|--------------------------------------|
| `timestamp missing/invalid` | 未传或格式错误                          | 重新传入整数秒级时间戳               |
| `timestamp expired`         | 客户端时间与服务器差值超限             | 同步客户端时间或放宽 offset          |
| `sign missing/invalid`      | 未传/计算错误/参数顺序不一致           | 按字典序 + 正确密钥重新计算           |
| `AES decrypt error`         | AES_KEY 长度、IV 不一致或密文被篡改    | 确保 16/24/32 位密钥且首 16 位做 IV   |
