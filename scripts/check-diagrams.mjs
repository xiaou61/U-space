#!/usr/bin/env node
/**
 * 校验文档里的 mermaid 图。
 *
 * 存在的理由只有一个：图由 AI 写，AI 写完必须能**自己确认语法是对的**。
 * 少了这一步，坏图的发现时点会推迟到某天有人打开文档——那时已经没人记得原意。
 *
 * 只调 `mermaid.parse`，不渲染、不落盘、不需要 chromium。渲染要 headless 浏览器
 * （几十秒 + 一次下载），而语法校验才是"写完立即可自检"的必要条件，也是能进内环的那部分。
 * 代价是**布局问题（挤、重叠）查不出来**——那只能靠人眼看图。
 *
 * 用法：
 *   node scripts/check-diagrams.mjs                    # 扫 docs/ 与 .agent/
 *   node scripts/check-diagrams.mjs docs/architecture  # 只扫指定目录
 *   node scripts/check-diagrams.mjs --quiet            # 只输出失败
 *
 * 退出码：0 全部可解析；1 有坏图；2 环境或参数有问题。
 */
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { createRequire } from 'node:module';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const FRONTEND = join(ROOT, 'frontend');
const SKIP_DIRS = new Set(['node_modules', '.git', '.worktrees', 'target', 'dist', 'output']);

const argv = process.argv.slice(2);
const quiet = argv.includes('--quiet');
const targets = argv.filter((a) => !a.startsWith('--'));
if (targets.length === 0) {
  targets.push('docs', '.agent');
}

/** 依赖装在 frontend/ 下（仓库唯一的 node 工程），所以从那里解析。 */
const req = createRequire(pathToFileURL(join(FRONTEND, 'package.json')));
let mermaid;
try {
  const { JSDOM } = await import(pathToFileURL(req.resolve('jsdom')).href);
  const dom = new JSDOM('<!doctype html><html><body></body></html>', { pretendToBeVisual: true });
  const define = (k, v) =>
    Object.defineProperty(globalThis, k, { value: v, writable: true, configurable: true });
  define('window', dom.window);
  // class / state / C4 图的解析路径会走 DOMPurify，没有 DOM 会误报 "DOMPurify.addHook is not a function"
  for (const k of ['document', 'navigator', 'Element', 'HTMLElement', 'SVGElement', 'Node', 'DOMParser', 'getComputedStyle', 'MutationObserver']) {
    define(k, dom.window[k]);
  }
  mermaid = (await import(pathToFileURL(req.resolve('mermaid')).href)).default;
  mermaid.initialize({ startOnLoad: false });
} catch (e) {
  console.error('无法加载 mermaid / jsdom（先在 frontend/ 跑 pnpm install）：', e.message);
  process.exit(2);
}

function walk(dir, out = []) {
  let entries;
  try {
    entries = readdirSync(dir, { withFileTypes: true });
  } catch {
    return out;
  }
  for (const entry of entries) {
    if (entry.isDirectory()) {
      if (!SKIP_DIRS.has(entry.name)) {
        walk(join(dir, entry.name), out);
      }
    } else if (entry.name.endsWith('.md')) {
      out.push(join(dir, entry.name));
    }
  }
  return out;
}

/** 取出所有 ```mermaid 围栏：startLine 是围栏那一行（1 基），code 是围栏内容。 */
function fences(file) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  const found = [];
  for (let i = 0; i < lines.length; i++) {
    if (!/^```\s*mermaid\b/i.test(lines[i].trim())) {
      continue;
    }
    let end = i + 1;
    while (end < lines.length && !/^```\s*$/.test(lines[end].trim())) {
      end++;
    }
    found.push({ startLine: i + 1, code: lines.slice(i + 1, end).join('\n'), lines });
    i = end;
  }
  return found;
}

const files = [];
for (const target of targets) {
  const abs = join(ROOT, target);
  let exists = true;
  try {
    statSync(abs);
  } catch {
    exists = false;
  }
  if (!exists) {
    console.error(`目录不存在：${target}`);
    process.exit(2);
  }
  walk(abs, files);
}

const failures = [];
let checked = 0;
for (const file of files) {
  for (const fence of fences(file)) {
    checked++;
    try {
      await mermaid.parse(fence.code);
    } catch (e) {
      // hash.loc.first_line 是围栏内的 1 基行号（报错文案里的行号有内部偏移，不可信）
      const rel = relative(ROOT, file).replace(/\\/g, '/');
      const offset = e.hash?.loc?.first_line;
      const line = offset === undefined ? fence.startLine : fence.startLine + offset;
      const source = offset === undefined ? '' : (fence.lines[line - 1] ?? '').trim();
      const reason = String(e.message).split('\n')[0].replace(/^Parse error on line \d+:\s*/, 'Parse error');
      failures.push({ rel, line, source, reason });
    }
  }
}

if (failures.length > 0) {
  for (const f of failures) {
    console.error(`✗ ${f.rel}:${f.line}  ${f.reason}`);
    if (f.source) {
      console.error(`      ${f.source}`);
    }
  }
  console.error(`\n${failures.length}/${checked} 个图块无法解析。`);
  process.exit(1);
}

if (!quiet) {
  console.log(`✓ ${checked} 个 mermaid 图块全部可解析（${files.length} 个文件）。`);
}
