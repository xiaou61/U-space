/**
 * e2e 的目标：入口路径与账号。
 *
 * <p>本地跑（不设环境变量）时，管理端就是 Vite 的根，账号取 `db/devdata` 的种子。
 *
 * <p>指向已部署实例时（`E2E_BASE_URL`，见 `playwright.config.ts`），这两样都要给，因为：
 * 部署形态是"一个 jar 同时供 API 与前端静态"，管理端在 `/admin/index.html` 而不是站点根
 * （站点根是学习者端）；而种子账号**不在**线上——口令写在公开仓库里，放在公网可达的实例上
 * 等于开放管理权限。
 *
 * <pre>
 * E2E_BASE_URL           站点根，例如 http://host:8081
 * E2E_ADMIN_PATH         管理端入口路径，默认 /  ；线上给 /admin/index.html
 * E2E_ADMIN_IDENTIFIER / E2E_ADMIN_PASSWORD
 * E2E_LEARNER_IDENTIFIER / E2E_LEARNER_PASSWORD
 * </pre>
 *
 * <p>口令只从环境变量进来，不落任何被跟踪的文件——与后端集成测试读测试库口令的做法一致。
 */
export const ADMIN_ENTRY = process.env.E2E_ADMIN_PATH ?? '/'

export const ADMIN = {
  identifier: process.env.E2E_ADMIN_IDENTIFIER ?? 'seed-admin',
  password: process.env.E2E_ADMIN_PASSWORD ?? 'seed-admin-password',
}

export const LEARNER = {
  identifier: process.env.E2E_LEARNER_IDENTIFIER ?? 'seed-learner',
  password: process.env.E2E_LEARNER_PASSWORD ?? 'seed-learner-password',
}
