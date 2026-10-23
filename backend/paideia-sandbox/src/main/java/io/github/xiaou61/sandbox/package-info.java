/**
 * 代码执行沙箱模块。
 *
 * <p><b>职责</b>：在加固容器里跑练习代码（Java / Python / SQL），只回实际输出、不判题、
 * 不存源码；SQL 数据集的白名单解析与治理（`sql_datasets` / `sql_dataset_reports`）；
 * 运行审计与配额（`code_runs`）。
 *
 * <p><b>对外契约只在包根</b>：目前只有 {@link io.github.xiaou61.sandbox.DatasetStorage}
 * （数据集内容的存储端口，由装配根桥接到 knowledge 的附件存储）。
 * `internal` 子包是内部实现，其它模块不得访问——由 Spring Modulith 在构建期拦截。
 *
 * <p><b>两条安全纪律</b>：
 * <ul>
 *   <li>**执行用户代码的唯一入口**，加固参数（无网络、只读根、非 root、cap 全丢、资源上限）
 *       写在 `DockerCommand` 里并由测试逐条断言——少写一个仍然能跑，只是隔离没了。</li>
 *   <li>**数据集解析必须带 userId**（`SqlDatasets.sql(long, String)`）：私人数据集只有本人能跑，
 *       这是本模块唯一的越权边界。</li>
 * </ul>
 */
package io.github.xiaou61.sandbox;
