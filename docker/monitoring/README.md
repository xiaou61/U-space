# Prometheus + Grafana 监控部署

## 快速启动

### 1. 启动监控服务

```bash
# 在此目录下执行
docker-compose up -d
```

### 2. 验证服务状态

```bash
# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问界面

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
  - 默认账号: `admin`
  - 默认密码: `admin123`

### 4. 确认应用监控

1. 访问 Prometheus Targets: http://localhost:9090/targets
2. 确认 `code-nest` 任务状态为 **UP**

## 配置说明

### prometheus.yml
- Prometheus 主配置文件
- 配置了采集间隔、目标等
- 如需修改目标地址，请编辑此文件

### alert_rules.yml
- 告警规则配置（当前未启用）
- 要启用告警，需要：
  1. 取消 prometheus.yml 中 `rule_files` 的注释
  2. 配置 AlertManager（可选）

### docker-compose.yml
- Docker Compose 编排文件
- 包含 Prometheus 和 Grafana 两个服务
- 数据会持久化到 Docker volumes

## Grafana 配置

### 添加数据源

1. 登录 Grafana
2. 左侧菜单 → Configuration → Data Sources
3. Add data source → Prometheus
4. URL 填写: `http://prometheus:9090`
5. Save & Test

### 导入 Dashboard

推荐以下 Dashboard ID：

- **11378** - Spring Boot 2.1 Statistics（全面监控）
- **4701** - JVM (Micrometer)（JVM专项）
- **6756** - Spring Boot Statistics（轻量级）

导入步骤：
1. 左侧菜单 → + → Import
2. 输入 Dashboard ID
3. 选择 Prometheus 数据源
4. Import

## 停止服务

```bash
docker-compose down
```

## 完全清理（包括数据）

```bash
docker-compose down -v
```

## 故障排查

### 应用连接失败

1. 确认应用运行在 `localhost:9999`
2. 确认 `/api/actuator/prometheus` 端点可访问
3. Windows 系统确认使用 `host.docker.internal`
4. Linux 系统可能需要改为 `172.17.0.1` 或宿主机IP

### Grafana 无数据

1. 检查 Prometheus 数据源配置
2. 检查 Prometheus Targets 状态
3. 检查应用指标端点是否正常

## 参考文档

详细配置说明请查看：`docs/Prometheus监控部署指南.md`

