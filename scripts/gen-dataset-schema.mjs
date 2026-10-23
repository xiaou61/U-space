#!/usr/bin/env node
/**
 * 生成内置数据集的 schema 快照（WORK-035 T-03）。
 *
 * <p><b>为什么不在运行时抓</b>：抓 schema 要起一个容器（3–5 秒 + 一次配额）。内置数据集
 * 在内容不变时 schema 也不会变，所以生成一次、提交进仓库，页面与补全只读这份文件。
 *
 * <p><b>为什么必须真跑一次而不是手写</b>：手写一定会和实际不符——列顺序、`decimal(10,2)`
 * 这种类型字符串、哪个列是主键，都是 MySQL 说了算。手写的快照看起来正常，但补全时会提示
 * 一个不存在的列，或者漏掉一个真实存在的列，而这两种错都没有报错。
 *
 * <p>用法（需要 `codenest-online` 可达、服务器上有沙箱镜像）：
 * ```
 * node scripts/gen-dataset-schema.mjs
 * ```
 * 输出写到 `backend/paideia-sandbox/src/main/resources/sandbox/datasets/<key>.schema.json`。
 *
 * <p>**脚本不进常规测试**：它要 SSH 到服务器并起容器，不是一个能在 CI 里跑的东西。
 * 改数据集内容、改镜像、或加新数据集时手动跑一次。
 */
import { execFileSync } from 'node:child_process'
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..')
const DATASETS = join(ROOT, 'backend/paideia-sandbox/src/main/resources/sandbox/datasets')

/** 与 `SqlDatasets.REGISTRY` 的 key 对应。加数据集时两边都要加。 */
const KEYS = ['orders-public', 'orders-hidden-a']

const IMAGE =
  'paideia-sandbox-mysql@sha256:e2eafb6ea161ddda420bde0f8d5ec3b0b03115811ea9507a23710034ef6d0a4f'

/** 容器内脚本：灌数据集，然后把 information_schema 的列信息以 XML 打出来。 */
const SCRIPT = `
set -e
cat > /tmp/dataset.sql
mkdir -p /tmp/nofiles
cp -a /opt/datadir-base /tmp/data && chmod -R u+w /tmp/data
mysqld --skip-networking --socket=/tmp/m.sock --datadir=/tmp/data --user=mysql \\
  --secure-file-priv=/tmp/nofiles --local-infile=0 --skip-log-bin \\
  --mysqlx=0 --pid-file=/tmp/data/mysqld.pid --log-error=/tmp/mysqld.log &
for i in $(seq 1 900); do
  mysqladmin --socket=/tmp/m.sock -uroot ping >/dev/null 2>&1 && break
  sleep 0.02
done
mysqladmin --socket=/tmp/m.sock -uroot ping >/dev/null 2>&1 || { tail -5 /tmp/mysqld.log >&2; exit 8; }
mysql --socket=/tmp/m.sock -uroot -e "CREATE DATABASE practice"
mysql --socket=/tmp/m.sock -uroot practice < /tmp/dataset.sql >/dev/null 2>/tmp/ds.err || { cat /tmp/ds.err >&2; exit 9; }
mysql --socket=/tmp/m.sock -uroot practice --xml -e "
SELECT c.TABLE_NAME AS tbl, c.COLUMN_NAME AS col, c.COLUMN_TYPE AS typ,
       c.IS_NULLABLE AS nullable, c.COLUMN_KEY AS colkey
FROM information_schema.COLUMNS c
WHERE c.TABLE_SCHEMA='practice'
ORDER BY c.TABLE_NAME, c.ORDINAL_POSITION"
`

/**
 * 解析 `mysql --xml` 的列信息输出。
 *
 * <p>与 `SqlResultCodec` 同一形态（`<field name="...">值</field>`），但这里是构建期脚本，
 * 不值得复用那套 Java 正则——用最简单的解析即可，出错时人工能看出来。
 */
function parseRows(xml) {
  const rows = []
  for (const block of xml.split('<row>').slice(1)) {
    const row = {}
    const field = /<field name="([^"]*)">([\s\S]*?)<\/field>/g
    let match
    while ((match = field.exec(block)) !== null) {
      row[match[1]] = match[2]
    }
    if (row.tbl) {
      rows.push(row)
    }
  }
  return rows
}

/** 列信息 → `{ tables: [{ name, columns: [{ name, type, nullable, primaryKey }] }] }`。 */
function toSchema(rows) {
  const tables = new Map()
  for (const row of rows) {
    if (!tables.has(row.tbl)) {
      tables.set(row.tbl, [])
    }
    tables.get(row.tbl).push({
      name: row.col,
      type: row.typ,
      nullable: row.nullable === 'YES',
      primaryKey: row.colkey === 'PRI',
    })
  }
  return {
    tables: [...tables.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([name, columns]) => ({ name, columns })),
    // 外键关系留空：内置数据集本来就没建外键（课程第 08 关专门讲了很多团队不用外键）。
    // 这里保留字段是为了让「作者手工标注关联」有地方落，不让前端去猜。
    relations: [],
  }
}

/**
 * 把脚本 base64 编码后交给远端解码执行。
 *
 * <p><b>为什么不用 `sh -c "<多行脚本>"`</b>：试过两次都不行，都是引号/换行在两层 shell
 * （本地 bash → SSH → 远端 sh）之间被反复解释。压成单行又会破坏 `cat > file` 与 heredoc
 * 的语义（`;` 拼接让后续命令的输出被写进文件）。base64 只有 `[A-Za-z0-9+/=]`，
 * 没有任何一个字符需要转义，是唯一不用调试的方案。
 */
function encode(script) {
  return Buffer.from(script, 'utf8').toString('base64')
}

for (const key of KEYS) {
  const sqlPath = join(DATASETS, `${key}.sql`)
  const sql = readFileSync(sqlPath, 'utf8')
  process.stdout.write(`生成 ${key} 的 schema… `)

  const encoded = encode(SCRIPT)
  const out = execFileSync(
    'ssh',
    [
      '-o', 'ConnectTimeout=8',
      'codenest-online',
      `echo ${encoded} | base64 -d > /tmp/probe.sh && ` +
        `docker run --rm -i --network=none --read-only --tmpfs /tmp:rw,size=768m,exec ` +
        `--user 999:999 --cap-drop=ALL --security-opt=no-new-privileges ` +
        `--memory=1024m --memory-swap=1024m --cpus=1 --pids-limit=128 ` +
        `--ulimit nofile=1024 --ulimit nproc=128 --stop-timeout=0 -e TZ=UTC ` +
        `-v /tmp/probe.sh:/probe.sh:ro --entrypoint sh ${IMAGE} /probe.sh`,
    ],
    { input: sql, maxBuffer: 10 * 1024 * 1024 },
  )

  const schema = toSchema(parseRows(out.toString()))
  if (schema.tables.length === 0) {
    throw new Error(`${key}: 没抓到任何表——数据集可能导入失败`)
  }
  writeFileSync(join(DATASETS, `${key}.schema.json`), `${JSON.stringify(schema, null, 2)}\n`)
  console.log(`${schema.tables.length} 张表 / ${schema.tables.reduce((n, t) => n + t.columns.length, 0)} 列`)
}
