#!/usr/bin/env python3
"""在**真实沙箱**上验两条只能真跑才成立的验收标准（WORK-032 AC-002 / AC-007）。

这两条本地验不了：AC-002 要容器真起真销毁，AC-007 要隐藏数据集真的在服务端。
本机没有 Docker，所以对着已部署实例跑。

为什么是脚本而不是单测：`SandboxIntegrationTest` 里 `CodeRunner` 是桩（本机没 Docker），
它验的是控制器那一层；而这两条验的是"容器与数据集"本身，绕不过真沙箱。
放进 e2e 也不合适——线上不保证沙箱开着，且它需要凭据。

用法：

    PAIDEIA_BASE_URL=http://<host>:<port> \\
    PAIDEIA_LEARNER_IDENTIFIER=... PAIDEIA_LEARNER_PASSWORD=... \\
    python scripts/verify_sql_live.py

凭据只从环境变量读，不写文件、不打印。
"""

from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from sql_judge import canonical_sha256  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 第 04 课那道题（按客户统计订单数）。题面与期望哈希都来自内容源，不在这里重抄。
CASE_ID = "my-04-write"


def fail(message: str) -> None:
    print(f"  ✗ {message}")
    sys.exit(1)


def http(method: str, url: str, body: dict | None = None, token: str | None = None) -> dict:
    data = None if body is None else json.dumps(body).encode("utf-8")
    request = urllib.request.Request(url, data=data, method=method)
    request.add_header("Content-Type", "application/json")
    if token is not None:
        request.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        fail(f"{method} {url} → HTTP {error.code}: {error.read()[:300]!r}")


def login(base: str) -> str:
    identifier = os.environ.get("PAIDEIA_LEARNER_IDENTIFIER")
    password = os.environ.get("PAIDEIA_LEARNER_PASSWORD")
    if not identifier or not password:
        print("缺少 PAIDEIA_LEARNER_IDENTIFIER / PAIDEIA_LEARNER_PASSWORD", file=sys.stderr)
        sys.exit(2)
    payload = http("POST", f"{base}/api/v1/auth/login",
                   {"identifier": identifier, "password": password})
    token = (payload.get("data") or {}).get("accessToken")
    if not token:
        fail(f"登录未返回 accessToken：{payload}")
    return token


def run_sql(base: str, token: str, code: str, dataset: str) -> dict:
    payload = http("POST", f"{base}/api/v1/sandbox/run",
                   {"language": "sql", "code": code, "dataset": dataset}, token)
    if payload.get("code") != 0:
        fail(f"运行被拒：{payload}")
    return payload["data"]


def load_case() -> dict:
    path = os.path.join(ROOT, "content", "courses", "mysql", "cases", "lessons.json")
    with open(path, encoding="utf-8") as handle:
        cases = json.load(handle)
    found = [item for item in cases if item.get("id") == CASE_ID]
    if not found:
        fail(f"内容源里找不到题目 {CASE_ID}")
    return found[0]


def check_isolation(base: str, token: str) -> None:
    """AC-002：同一用户连续两次运行，第二次看不到第一次建的表。"""
    print("AC-002 跨次隔离（干净实例）")
    first = run_sql(base, token,
                    "CREATE TABLE probe_zz (v INT); INSERT INTO probe_zz VALUES (42); "
                    "SELECT v FROM probe_zz;",
                    "orders-public")
    tables = first.get("tables") or []
    if not tables or tables[0]["rows"] != [["42"]]:
        fail(f"第一次运行没能建表读到 42：{tables}")
    print("  ✓ 第一次：建表并读到 42")

    second = run_sql(base, token, "SELECT v FROM probe_zz;", "orders-public")
    stderr = second.get("stderr") or ""
    if "doesn't exist" not in stderr and "does not exist" not in stderr:
        fail(f"第二次仍能看到上次建的表——实例没有隔离。stderr={stderr!r}")
    print("  ✓ 第二次：表不存在（实例已销毁，状态未跨次保留）")


def check_hardcode_rejected(base: str, token: str) -> None:
    """AC-007：把公开集的结果硬编码，在隐藏数据集上必须判错。"""
    print("AC-007 硬编码在隐藏数据集上被判错")
    case = load_case()
    normalize = case["normalize"]
    expect = case["expectHash"]

    reference = run_sql(base, token, case["reference"], "orders-public")
    tables = reference.get("tables") or []
    if not tables:
        fail("参考解没有返回结果集")
    actual = canonical_sha256(tables[0], normalize)
    if actual != expect["orders-public"]:
        fail(f"参考解在公开集上的哈希与题面不符：算出 {actual}，题面 {expect['orders-public']}")
    print("  ✓ 参考解在公开集上与 expectHash 一致（判题口径对得上）")

    # 把公开集的真实结果原样写成字面量——这就是"硬编码答案"最直接的形态
    rows = tables[0]["rows"]
    columns = tables[0]["columns"]
    selects = []
    for row in rows:
        cells = []
        for name, value in zip(columns, row):
            literal = f"'{value}'" if case["normalize"]["types"].get(name) == "string" else value
            cells.append(f"{literal} AS {name}")
        selects.append("SELECT " + ", ".join(cells))
    hardcoded = " UNION ALL ".join(selects)
    print(f"  硬编码写法：{hardcoded}")

    hidden = run_sql(base, token, hardcoded, "orders-hidden-a")
    hidden_tables = hidden.get("tables") or []
    if not hidden_tables:
        fail("硬编码写法在隐藏集上没有返回结果集")
    hardcoded_hash = canonical_sha256(hidden_tables[0], normalize)
    if hardcoded_hash == expect["orders-hidden-a"]:
        fail("硬编码在隐藏数据集上竟然判对了——隐藏集没有起作用")
    print("  ✓ 硬编码在隐藏数据集上判错（隐藏集内容与公开集不同）")


def main() -> int:
    base = os.environ.get("PAIDEIA_BASE_URL", "").rstrip("/")
    if not base:
        print("缺少 PAIDEIA_BASE_URL", file=sys.stderr)
        return 2
    token = login(base)
    print(f"已登录 {base}\n")
    check_isolation(base, token)
    print()
    check_hardcode_rejected(base, token)
    print("\n两条验收标准均成立（真实沙箱，非桩）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
