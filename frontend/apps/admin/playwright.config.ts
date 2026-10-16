import { defineConfig, devices } from '@playwright/test'

const BACKEND_DIR = '../../../backend'
const BACKEND_JAR = 'paideia-app/target/paideia-app-0.0.1-SNAPSHOT.jar'

/**
 * 指向已部署实例时（改完直接部署、再在线上跑一遍这条路径），不需要本地起任何东西：
 * 设了 `E2E_BASE_URL` 就跳过 webServer，直接打那个地址。账号随之由环境变量给出
 * （见 `e2e/accounts.ts`）。
 */
const REMOTE_BASE_URL = process.env.E2E_BASE_URL

/**
 * 管理端的端到端检查。
 *
 * <p>默认（不设 `E2E_BASE_URL`）在本地同时拉起后端：要证明的是"徽标显示的是接口真实返回"，
 * 只起前端会拿到代理错误，验证不了这一点。
 *
 * <p>前置条件：后端 jar 已构建，且到服务器 MySQL 的本地隧道（127.0.0.1:3307）是通的。
 * **指向线上时这两条都不需要**——线上那个进程本身连着库。
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  use: {
    baseURL: REMOTE_BASE_URL ?? 'http://127.0.0.1:5174',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: REMOTE_BASE_URL
    ? undefined
    : [
        {
          command: `java -jar ${BACKEND_JAR} --spring.profiles.active=local`,
          cwd: BACKEND_DIR,
          // 与管理端的说明一致：数据源指向 paideia_test，并额外加载 db/devdata 里的种子账号
          env: {
            // forceConnectionTimeZoneToSession 与 connectionTimeZone 必须成对（服务器 MySQL 会话是 +08:00）：
            // 少了它，建表默认值写进去的是本地钟点，而驱动按 UTC 解释，时间整体快 8 小时。
            SPRING_DATASOURCE_URL:
              'jdbc:mysql://36.212.5.130:3306/paideia_test?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&characterEncoding=utf8',
              // 直连服务器 MySQL（2026-10-16 起）：3306 已对外开放，**不再需要本地 3307 隧道**。
              // 账号只授权 paideia_test.*（生产库 paideia 没有任何可远程登录的账号）；
              // 口令只从环境变量来：先 `source backend/config/remote-db.local.env` 再跑 e2e。
              SPRING_DATASOURCE_USERNAME: 'paideia_test',
              SPRING_DATASOURCE_PASSWORD: process.env.PAIDEIA_TEST_DB_PASSWORD ?? '',
            SPRING_FLYWAY_LOCATIONS: 'classpath:db/migration,classpath:db/devdata',
            // 与管理端说明一致：paideia_test 被三种迁移配置共用，夹具的 V900 需要被忽略
            SPRING_FLYWAY_IGNORE_MIGRATION_PATTERNS: '*:future,*:missing',
            SPRING_FLYWAY_OUT_OF_ORDER: 'true',
            // 管理端的来源必须在后端白名单里。**即使走 Vite 代理也一样**：
            // 浏览器对同源的 POST 也会带 Origin 头，后端按来源判定，未列入直接 403。
            // Actuator 端点有独立的一份 CORS 配置，两处都要给。
            PAIDEIA_WEB_CORS_ALLOWED_ORIGINS: 'http://127.0.0.1:5174,http://localhost:5174',
            MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWED_ORIGINS: 'http://127.0.0.1:5174,http://localhost:5174',
          },
          url: 'http://127.0.0.1:8080/actuator/health',
          reuseExistingServer: true,
          timeout: 120_000,
        },
        {
          command: 'pnpm dev',
          url: 'http://127.0.0.1:5174',
          reuseExistingServer: true,
          timeout: 60_000,
        },
      ],
})
