#!/usr/bin/env python3
"""SQL 结果集判题口径的 Python 侧（WORK-032 TASK-004）。

与 `frontend/packages/ui/src/markdown/renderers/sql-judge.ts` **各自实现**，
共同由 `tests/sql-judge/corpus.json` 对照。两侧不共享代码是刻意的：共享一处就不会漂移，
但会把"判题规则"和"内容门禁"耦合到必须同语言；分开写则需要语料兜住漂移。

内容门禁（`verify_course.py`）用这一侧：
* 跑参考解得到期望结果；
* 断言 `alternatives` 与期望相同（证明判题不误杀等价写法）；
* 断言 `antiPatterns` 至少在一处不同（证明这题能区分真假）。

自检：

    python scripts/sql_judge.py --self-check
"""

from __future__ import annotations

import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CORPUS = ROOT / "tests" / "sql-judge" / "corpus.json"

# NULL 的固定标记。不能用空串——NULL 与 '' 是两回事。
NULL_MARKER = "\x00NULL"

COLUMN_SEP = "\x1f"
ROW_SEP = "\x1e"

TYPES = ("string", "int", "decimal", "datetime")


def normalize_cell(value: str, cell_type: str = "string") -> str:
    """按类型归一单个值。

    字符串列**不做 strip**：尾部空格在 VARCHAR 里是有意义的值，替用户裁掉等于
    把一处真实的差异藏起来。日期与数值才需要归一。
    """
    if value == NULL_MARKER:
        return NULL_MARKER
    if cell_type == "int":
        return _normalize_int(value)
    if cell_type == "decimal":
        return _normalize_decimal(value)
    if cell_type == "datetime":
        return _normalize_datetime(value)
    return value


def _normalize_int(raw: str) -> str:
    text = raw.strip()
    if not re.fullmatch(r"[+-]?\d+", text):
        return raw
    negative = text.startswith("-")
    digits = re.sub(r"^[+-]", "", text)
    digits = re.sub(r"^0+(?=\d)", "", digits)
    if negative and re.search(r"[1-9]", digits):
        return "-" + digits
    return digits


def _normalize_decimal(raw: str) -> str:
    """定点数按十进制字符串归一，**不经过浮点**。

    `0.1 + 0.2` 在二进制里是无限循环，用浮点归一化会让「金额精度」那一课自己打自己的脸。
    """
    text = raw.strip()
    if not re.fullmatch(r"[+-]?(\d+(\.\d*)?|\.\d+)", text):
        return raw
    negative = text.startswith("-")
    body = re.sub(r"^[+-]", "", text)
    if "." in body:
        integer, fraction = body.split(".", 1)
    else:
        integer, fraction = body, ""
    integer = re.sub(r"^0+(?=\d)", "", integer) or "0"
    fraction = re.sub(r"0+$", "", fraction)
    unsigned = integer if fraction == "" else f"{integer}.{fraction}"
    if negative and unsigned != "0":
        return "-" + unsigned
    return unsigned


def _normalize_datetime(raw: str) -> str:
    """日期时间归一成 `YYYY-MM-DD HH:MM:SS`；日期按当天零点。"""
    text = raw.strip().replace("T", " ")
    match = re.fullmatch(r"(\d{4}-\d{2}-\d{2})(?:[ ](\d{2}):(\d{2})(?::(\d{2})(?:\.\d+)?)?)?", text)
    if match is None:
        return raw
    day = match.group(1)
    hour = match.group(2) or "00"
    minute = match.group(3) or "00"
    second = match.group(4) or "00"
    return f"{day} {hour}:{minute}:{second}"


def canonicalize(table: dict, normalize: dict | None = None) -> str:
    """规范序列化。格式是两侧的硬契约：

    列名（默认按名排序）以 \\x1f 连接 + "\\n" +
    各行各格以 \\x1f 连接、行与行以 \\x1e 连接（行序忽略时按行串排序）。
    """
    normalize = normalize or {}
    columns: list[str] = list(table.get("columns") or [])
    rows: list[list[str]] = [list(row) for row in (table.get("rows") or [])]
    order = _column_order(columns, normalize)
    types = normalize.get("types") or {}

    ordered_columns = [columns[index] for index in order]
    ordered_rows = [
        [normalize_cell(row[index] if index < len(row) else NULL_MARKER, types.get(columns[index], "string"))
         for index in order]
        for row in rows
    ]
    if normalize.get("rowOrder") != "strict":
        ordered_rows.sort(key=lambda row: COLUMN_SEP.join(row))
    body = ROW_SEP.join(COLUMN_SEP.join(row) for row in ordered_rows)
    return COLUMN_SEP.join(ordered_columns) + "\n" + body


def canonical_sha256(table: dict, normalize: dict | None = None) -> str:
    return hashlib.sha256(canonicalize(table, normalize).encode("utf-8")).hexdigest()


def judge_sql(expected: dict, actual: dict, normalize: dict | None = None) -> bool:
    """判题：按同一口径归一后比较（行序/列序/类型见 corpus 的用例）。"""
    normalize = normalize or {}
    expected_columns: list[str] = list(expected.get("columns") or [])
    actual_columns: list[str] = list(actual.get("columns") or [])
    pairing = _pair_columns(expected_columns, actual_columns, normalize.get("columnOrder") == "strict")
    if pairing is None:
        return False
    expected_rows = expected.get("rows") or []
    actual_rows = actual.get("rows") or []
    if len(expected_rows) != len(actual_rows):
        return False

    types = normalize.get("types") or {}

    def expected_row(row: list[str]) -> list[str]:
        return [normalize_cell(row[i] if i < len(row) else NULL_MARKER, types.get(expected_columns[i], "string"))
                for i in range(len(expected_columns))]

    def actual_row(row: list[str]) -> list[str]:
        return [normalize_cell(row[pairing[i]] if pairing[i] < len(row) else NULL_MARKER,
                               types.get(expected_columns[i], "string"))
                for i in range(len(expected_columns))]

    left = [expected_row(row) for row in expected_rows]
    right = [actual_row(row) for row in actual_rows]
    if normalize.get("rowOrder") == "strict":
        return all(a == b for a, b in zip(left, right))
    left.sort(key=lambda row: COLUMN_SEP.join(row))
    right.sort(key=lambda row: COLUMN_SEP.join(row))
    return all(a == b for a, b in zip(left, right))


def _column_order(columns: list[str], normalize: dict) -> list[int]:
    order = list(range(len(columns)))
    if normalize.get("columnOrder") == "strict":
        return order
    return sorted(order, key=lambda index: (columns[index], index))


def _pair_columns(expected: list[str], actual: list[str], strict_order: bool) -> list[int] | None:
    if len(expected) != len(actual):
        return None
    if strict_order:
        if all(name == actual[index] for index, name in enumerate(expected)):
            return list(range(len(expected)))
        return None
    remaining: dict[str, list[int]] = {}
    for index, name in enumerate(actual):
        remaining.setdefault(name, []).append(index)
    pairing: list[int] = []
    for name in expected:
        bucket = remaining.get(name)
        if not bucket:
            return None
        pairing.append(bucket.pop(0))
    return pairing


def run_corpus() -> int:
    """自检：用 corpus 逐条对照。返回失败条数。"""
    if not CORPUS.exists():
        print(f"找不到语料：{CORPUS}", file=sys.stderr)
        return 1
    corpus = json.loads(CORPUS.read_text(encoding="utf-8"))
    if corpus.get("nullMarker") != NULL_MARKER:
        print(f"语料声明的 NULL 标记与实现不一致：{corpus.get('nullMarker')!r}", file=sys.stderr)
        return 1
    failures = 0
    for item in corpus["cases"]:
        got = judge_sql(item["expected"], item["actual"], item.get("normalize") or {})
        if got != item["pass"]:
            failures += 1
            print(f"FAIL {item['name']}：期望 {item['pass']}，实际 {got}", file=sys.stderr)
        canonical = item.get("canonical")
        if canonical is not None:
            actual_canonical = canonicalize(item["expected"], item.get("normalize") or {})
            if actual_canonical != canonical:
                failures += 1
                print(f"FAIL {item['name']}：规范串不一致\n  期望 {canonical!r}\n  实际 {actual_canonical!r}",
                      file=sys.stderr)
    print(f"语料 {len(corpus['cases'])} 条，失败 {failures} 条")
    return failures


def main(argv: list[str]) -> int:
    if "--self-check" in argv:
        return 1 if run_corpus() else 0
    print("用法：python scripts/sql_judge.py --self-check", file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
