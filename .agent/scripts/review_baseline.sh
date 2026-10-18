#!/usr/bin/env bash
# 审查基线自检：只看两件事——基线是否还存在、离 HEAD 差了多少。
#
# 为什么要脚本：2026-10-18 那次我用眼睛数 `git log` 的输出来报"85 个提交"，
# 实际 `rev-list --count` 是 68。数字必须来自命令。
#
# 用法：.agent/scripts/review_baseline.sh
set -euo pipefail

BASELINE_FILE=".agent/reviews/BASELINE"
[ -f "$BASELINE_FILE" ] || { echo "缺少 $BASELINE_FILE" >&2; exit 1; }

BASE=$(sed -n 's/^reviewed_through:[[:space:]]*//p' "$BASELINE_FILE" | tail -1)
[ -n "$BASE" ] || { echo "BASELINE 里没有 reviewed_through" >&2; exit 1; }

git rev-parse --verify --quiet "$BASE^{commit}" >/dev/null \
  || { echo "基线 $BASE 在仓库里不存在（历史被重写过？）" >&2; exit 1; }

if ! git merge-base --is-ancestor "$BASE" HEAD; then
  echo "基线 $BASE 不是 HEAD 的祖先：历史被重写过，之前的审查结论需要重做" >&2
  exit 1
fi

AHEAD=$(git rev-list --count "$BASE"..HEAD)
echo "已审到 $BASE，距 HEAD 还有 $AHEAD 个提交"
[ "$AHEAD" -eq 0 ] && echo "当前 HEAD 已全覆盖" || echo "下一个区间：$BASE..HEAD"
