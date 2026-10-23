import { Alert, AlertDescription, AlertTitle, Badge } from '@paideia/ui'

import { parseExplainTree, summarize, type ExplainNode } from './explain'

/**
 * 执行计划树（WORK-035 T-05）。
 *
 * <p><b>为什么不用结果表组件渲染</b>：实测（2026-10-23，真实镜像）确认
 * `mysql --xml` 把整棵 `EXPLAIN FORMAT=TREE` 塞进**一个字段值**里，拿到的是 1 列 × 1 行，
 * 多行之间是 `\n`。结构信息不在结果集里，只能从文本还原成树。
 *
 * <p><b>标注是按节点的</b>：一条查询可以 Filter 在上、Table scan 在下。给整条贴一个标签
 * 会把"下面那层是全表扫"这件事藏起来，而那正是用户最该看到的。
 */
export function ExplainTree({ output }: { output: string }) {
  const nodes = parseExplainTree(output)
  if (nodes.length === 0) {
    return (
      <Alert>
        <AlertTitle>没有执行计划</AlertTitle>
        <AlertDescription>这条语句没有返回执行计划（可能不是查询语句）。</AlertDescription>
      </Alert>
    )
  }
  const summary = summarize(nodes)

  return (
    <div className="space-y-3" data-testid="explain-tree">
      <div className="flex flex-wrap items-center gap-2 text-sm">
        {summary.hasIndex ? <Badge>走了索引</Badge> : null}
        {summary.hasScan ? <Badge variant="destructive">有全表扫描</Badge> : null}
        {summary.maxRows === null ? null : (
          <span className="text-muted-foreground text-xs">估算最多扫描 {summary.maxRows} 行</span>
        )}
      </div>

      <ul className="border-border space-y-1 rounded-lg border p-3 font-mono text-[12.5px]">
        {nodes.map((node, index) => (
          <ExplainRow key={index} node={node} />
        ))}
      </ul>

      {summary.hasScan ? (
        <p className="text-muted-foreground text-sm">
          出现 <code className="font-mono">Table scan</code> 说明这一层在逐行读整张表。
          如果条件列上有索引，检查是不是被函数包住、类型不匹配，或者复合索引违反了最左前缀。
        </p>
      ) : null}
    </div>
  )
}

function ExplainRow({ node }: { node: ExplainNode }) {
  // 缩进用 padding 而不是嵌套 <ul>：MySQL 的 TREE 本来就是扁平的缩进文本，
  // 还原成嵌套结构要多一层递归，而视觉上二者等价。
  return (
    <li
      className="flex items-start gap-2 leading-6"
      style={{ paddingLeft: `${node.depth * 20}px` }}
      data-access={node.access}
    >
      <span className="text-muted-foreground/50 shrink-0 select-none">→</span>
      <span className={accessClass(node.access)}>{node.text}</span>
      {node.access === 'scan' ? (
        <Badge variant="destructive" className="shrink-0">
          全表扫
        </Badge>
      ) : null}
      {node.access === 'index' ? (
        <Badge className="shrink-0">索引</Badge>
      ) : null}
    </li>
  )
}

function accessClass(access: ExplainNode['access']): string {
  if (access === 'scan') {
    return 'text-destructive'
  }
  if (access === 'index') {
    return 'text-primary'
  }
  return ''
}
