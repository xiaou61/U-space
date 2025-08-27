# RestTemplate 跨模块调用使用说明

## 📚 什么是 RestTemplate

RestTemplate 是 Spring 框架提供的用于调用 REST API 的客户端工具，可以方便地进行 HTTP 请求。在我们的项目中，用于活动模块调用 auth 模块的 API 获取学生姓名。

## 🏗️ 架构流程

```
活动模块业务代码 
    ↓
UserNameService.getUserNameById()
    ↓
RestTemplate HTTP调用
    ↓
auth模块 API: /user/student/internal/name/{studentId}
    ↓
返回学生姓名
```

## ⚙️ 配置说明

### 1. RestTemplate Bean 配置
```java
// xiaou-modules-activity/src/main/java/com/xiaou/activity/config/RestTemplateConfig.java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 2. 服务地址配置
```yaml
# application-dev.yml
auth:
  service:
    url: http://localhost:8080  # auth模块的服务地址
```

## 💻 使用方式

### 在业务代码中注入和使用：

```java
@Service
public class SomeActivityService {
    
    @Autowired
    private UserNameService userNameService;
    
    public void someMethod() {
        String userId = "student123";
        
        // 获取学生姓名（自动处理缓存和API调用）
        String studentName = userNameService.getUserNameById(userId);
        
        System.out.println("学生姓名: " + studentName);
    }
}
```

## 🔧 工作机制

### 1. 缓存机制
- **缓存时间**：30分钟
- **缓存键**：`user:name:{userId}`
- **缓存优先**：先查缓存，未命中再调用API

### 2. API调用
- **请求URL**：`{authServiceUrl}/user/student/internal/name/{studentId}`
- **请求方式**：GET
- **返回格式**：`R<String>` 统一响应格式

### 3. 容错处理
- API调用失败时返回默认格式：`用户{前8位ID}`
- 不会影响主业务流程

## 🚨 可能遇到的问题

### 1. 服务地址配置错误
**问题**：调用失败，返回默认用户名格式
**解决**：检查 `application-dev.yml` 中的 `auth.service.url` 配置

### 2. auth 模块服务未启动
**问题**：连接超时或拒绝连接
**解决**：确保 auth 模块服务正在运行

### 3. 网络连接问题
**问题**：HTTP 请求超时
**解决**：检查网络连接，可以添加超时配置

## 🧪 测试方法

### 1. 单元测试
```java
@Test
public void testGetUserName() {
    String userId = "existing_student_id";
    String name = userNameService.getUserNameById(userId);
    assertNotNull(name);
    assertNotEquals("未知用户", name);
}
```

### 2. 手动测试API
```bash
# 测试 auth 模块 API 是否正常
curl http://localhost:8080/user/student/internal/name/your_student_id
```

### 3. 查看日志
日志会记录：
- 缓存命中/未命中
- API调用结果
- 异常信息

## 📈 性能优化

### 1. 缓存策略
- 正常姓名缓存 30 分钟
- 默认格式也会缓存，避免重复失败调用

### 2. 连接池优化（可选）
如果调用频繁，可以配置连接池：

```java
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    
    // 配置连接池
    HttpComponentsClientHttpRequestFactory factory = 
        new HttpComponentsClientHttpRequestFactory();
    factory.setConnectionRequestTimeout(5000);
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(10000);
    
    restTemplate.setRequestFactory(factory);
    return restTemplate;
}
```

## 📝 日志监控

### 查看调用日志
```bash
# 查看活动模块日志
tail -f logs/activity.log | grep "用户.*姓名"

# 查看 auth 模块日志  
tail -f logs/auth.log | grep "internal/name"
```

## 🔮 扩展说明

这个方案可以很容易扩展到其他跨模块调用场景：
1. 获取班级信息
2. 获取课程信息  
3. 获取用户权限等

只需要在对应模块提供 API，然后在消费模块中类似调用即可。 