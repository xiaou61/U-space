#!/usr/bin/env node
/**
 * 从 pom 生成后端模块依赖图。
 *
 * **这类图不让 AI 画**：模块有几个、谁依赖谁，答案在 pom 里，不在语义里。
 * 让模型凭印象复述一定会漏会错，而且错得很像真的。脚本产出的这张图永远是对的。
 * 叙事性的图（请求链路、状态机、业务流程）才由 AI 写 mermaid。
 *
 * 用法：
 *   node scripts/gen-arch-diagram.mjs          # 重写 docs/architecture/01-module-dependencies.md
 *   node scripts/gen-arch-diagram.mjs --check  # 只比对，不一致则退出码 1（防漂移）
 */
import { readFileSync, writeFileSync, existsSync, mkdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const BACKEND = join(ROOT, 'backend');
const OUT = join(ROOT, 'docs', 'architecture', '01-module-dependencies.md');
const OWN_GROUP = 'io.github.xiaou61';

const block = (xml, tag) => {
  const m = xml.match(new RegExp(`<${tag}>[\\s\\S]*?</${tag}>`));
  return m ? m[0] : '';
};

/** 只取 <dependencies> 里的本仓模块，且排除 dependencyManagement / build 里的同名块。 */
function moduleDeps(pomPath, self) {
  let xml;
  try {
    xml = readFileSync(pomPath, 'utf8');
  } catch {
    return [];
  }
  // 移除会被误当成"模块依赖"的区块（dependencyManagement 是声明、build 是插件依赖）
  xml = xml.replace(/<dependencyManagement>[\s\S]*?<\/dependencyManagement>/g, '');
  xml = xml.replace(/<build>[\s\S]*?<\/build>/g, '');
  const deps = new Set();
  for (const dep of block(xml, 'dependencies').matchAll(/<dependency>([\s\S]*?)<\/dependency>/g)) {
    const body = dep[1];
    const group = body.match(/<groupId>([^<]+)<\/groupId>/)?.[1]?.trim();
    const artifact = body.match(/<artifactId>([^<]+)<\/artifactId>/)?.[1]?.trim();
    if (group === OWN_GROUP && artifact && artifact !== self) {
      // 同一模块常被声明两次（正式依赖 + test-jar 测试夹具）。依赖图上是一条边。
      deps.add(artifact);
    }
  }
  return [...deps];
}

const rootPom = readFileSync(join(BACKEND, 'pom.xml'), 'utf8');
// 模块声明顺序本身就是"由底向上"的（platform 在前、app 在最后），保留它让布局贴近分层
const modules = [...block(rootPom, 'modules').matchAll(/<module>([^<]+)<\/module>/g)].map((m) => m[1].trim());
if (modules.length === 0) {
  console.error('backend/pom.xml 里没有 <modules>，无法生成。');
  process.exit(2);
}

const edges = [];
const rows = [];
for (const module of modules) {
  const deps = moduleDeps(join(BACKEND, module, 'pom.xml'), module);
  for (const dep of deps) {
    edges.push([module, dep]);
  }
  rows.push({ module, deps });
}

const id = (module) => module.replace(/^paideia-/, '');
const lines = [];
lines.push('<!--');
lines.push('  本文件由 scripts/gen-arch-diagram.mjs 生成，**不要手改**。');
lines.push('  手改会让 `node scripts/gen-arch-diagram.mjs --check` 失败（那是刻意的：这张图必须等于 pom 的事实）。');
lines.push('  要改图就改 pom 或改生成脚本，然后重跑生成。');
lines.push('-->');
lines.push('');
lines.push('# 后端模块依赖');
lines.push('');
lines.push(`由 \`backend/pom.xml\` 的 ${modules.length} 个模块生成。箭头方向是**依赖方 → 被依赖方**，`);
lines.push('所以箭头指向的模块可以不知道调用者的存在（依赖只许单向下行，`spring-modulith` 在构建期挡循环）。');
lines.push('');
lines.push('```mermaid');
lines.push('flowchart TB');
for (const { module } of rows) {
  lines.push(`  ${id(module)}["${module}"]`);
}
for (const [from, to] of edges) {
  lines.push(`  ${id(from)} --> ${id(to)}`);
}
lines.push('```');
lines.push('');
lines.push('## 各模块的直接依赖');
lines.push('');
lines.push('| 模块 | 直接依赖 |');
lines.push('| --- | --- |');
for (const { module, deps } of rows) {
  lines.push(`| \`${module}\` | ${deps.length === 0 ? '（无）' : deps.map((d) => `\`${d}\``).join('、')} |`);
}
lines.push('');

const output = lines.join('\n');
const mode = process.argv.includes('--check');
if (mode) {
  const current = existsSync(OUT) ? readFileSync(OUT, 'utf8') : '';
  if (current !== output) {
    console.error('✗ docs/architecture/01-module-dependencies.md 与 pom 不一致（已漂移）。');
    console.error('  跑 `node scripts/gen-arch-diagram.mjs` 重新生成，再连同代码一起提交。');
    process.exit(1);
  }
  console.log(`✓ 模块依赖图与 pom 一致（${modules.length} 个模块 / ${edges.length} 条依赖）。`);
} else {
  mkdirSync(dirname(OUT), { recursive: true });
  writeFileSync(OUT, output, 'utf8');
  console.log(`✓ 已写出 docs/architecture/01-module-dependencies.md（${modules.length} 个模块 / ${edges.length} 条依赖）。`);
}
