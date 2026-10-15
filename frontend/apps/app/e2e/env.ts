/**
 * e2e 的账号：默认取 `db/devdata` 的种子（只在本地跑时存在），
 * 指向已部署实例时（`E2E_BASE_URL`，见 `playwright.config.ts`）必须由环境变量给出——
 * 种子口令写在**公开仓库**里，放到公网可达的实例上等于开放权限。
 *
 * <p>口令只从环境变量进来，不落任何被跟踪的文件——与后端集成测试读测试库口令同一做法。
 */
export const LEARNER = {
  identifier: process.env.E2E_LEARNER_IDENTIFIER ?? 'seed-learner',
  password: process.env.E2E_LEARNER_PASSWORD ?? 'seed-learner-password',
}
