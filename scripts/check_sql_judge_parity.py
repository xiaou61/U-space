#!/usr/bin/env python3
"""两侧判题对照（WORK-032 TASK-004，AC-014）。

`sql_judge.py` 的 `--self-check` 只证明 **Python 自己** 与语料一致；TS 侧有它自己的 vitest。
但"两侧结论一致"这件事需要一个能**直接跑**的命令，否则它只能靠"我记得两边都跑过"。

做法：让 Node 用同一份语料跑 TS 实现，把每条结论打成一个 JSON，本脚本逐条比对。

    node 侧由 `frontend/packages/ui/scripts/sql-judge-report.mjs` 产出
    python 侧用本文件的 judge_sql

用法：

    python scripts/check_sql_judge_parity.py
"""

from __future__ import annotations

import json
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
UI_DIR = ROOT / "frontend" / "packages" / "ui"
REPORT_SCRIPT = UI_DIR / "scripts" / "sql-judge-report.mjs"
CORPUS = ROOT / "tests" / "sql-judge" / "corpus.json"

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sql_judge  # noqa: E402


def main() -> int:
    node = shutil.which("node")
    if node is None:
        print("跳过：本机没有 node", file=sys.stderr)
        return 0
    if not REPORT_SCRIPT.exists():
        print(f"找不到 {REPORT_SCRIPT}", file=sys.stderr)
        return 2

    result = subprocess.run([node, str(REPORT_SCRIPT)], capture_output=True, text=True,
                            encoding="utf-8", cwd=str(UI_DIR))
    if result.returncode != 0:
        print(result.stdout, file=sys.stderr)
        print(result.stderr, file=sys.stderr)
        return 2
    try:
        ts_verdicts = json.loads(result.stdout)
    except json.JSONDecodeError as error:
        print(f"TS 报告不是合法 JSON：{error}\n{result.stdout[:500]}", file=sys.stderr)
        return 2

    corpus = json.loads(CORPUS.read_text(encoding="utf-8"))
    mismatches = 0
    for item in corpus["cases"]:
        name = item["name"]
        if name not in ts_verdicts:
            print(f"MISMATCH {name}：TS 侧没有这条结论", file=sys.stderr)
            mismatches += 1
            continue
        py_pass = sql_judge.judge_sql(item["expected"], item["actual"], item.get("normalize") or {})
        py_canonical = sql_judge.canonicalize(item["expected"], item.get("normalize") or {})
        ts = ts_verdicts[name]
        if ts["pass"] != py_pass:
            print(f"MISMATCH {name}：判定不一致 py={py_pass} ts={ts['pass']}", file=sys.stderr)
            mismatches += 1
        if ts["canonical"] != py_canonical:
            print(f"MISMATCH {name}：规范串不一致\n  py={py_canonical!r}\n  ts={ts['canonical']!r}",
                  file=sys.stderr)
            mismatches += 1

    print(f"两侧对照 {len(corpus['cases'])} 条，不一致 {mismatches} 条")
    return 1 if mismatches else 0


if __name__ == "__main__":
    sys.exit(main())
