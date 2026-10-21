#!/usr/bin/env python3
"""SQL 题目的质量门禁（WORK-032 TASK-005）。

这个文件存在的唯一理由：**把"题目好不好"变成能被脚本否掉的断言。**

每道可执行题必须同时满足三条，缺一条就不合格：

1. **`reference` 能跑通**：期望结果由它在数据集上真跑出来，不手写。
   手写的结果表必然与数据集脱节，而且脱节了没人会发现。
2. **每个 `alternatives` 的结果都与 `reference` 相同**：证明判题不误杀等价写法。
   SQL 一题多解是常态——`JOIN` 与子查询、`EXISTS` 与 `LEFT JOIN` 都得算对。
   这条失败的真实后果是"学习者写对了却判错"，比报错更伤。
3. **每个 `antiPatterns` 至少在一个数据集上结果不同**：证明这道题能区分真假。
   全部通过的 antiPattern 意味着"任何写法都对"，那这题在骗人。

另外检查 `goal` / `trap` 非空，且 `trap` 能在该课「常见误区」里找到对应条目——
防止出超纲题：课里没讲的坑不许拿来考。

自检：

    python scripts/sql_cases.py --check            # 校验全部题目
    python scripts/sql_cases.py --check --course 03
    python scripts/sql_cases.py --write-hashes     # 回写 expectHash
"""

from __future__ import annotations

import argparse
import glob
import json
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
COURSE_DIR = ROOT / "content" / "courses" / "mysql"
DATASET_DIR = COURSE_DIR / "datasets"
CASES_DIR = COURSE_DIR / "cases"

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sql_judge  # noqa: E402

EXECUTABLE_KINDS = ("sql-write", "sql-fix")
READONLY_KINDS = ("sql-predict", "sql-diagnose", "sql-blank")
ALL_KINDS = EXECUTABLE_KINDS + READONLY_KINDS

failures: list[str] = []


def fail(case: str, message: str) -> None:
    failures.append(f"{case}: {message}")


# ── 执行 ────────────────────────────────────────────────────────────────


def _connect():
    """连本机 MySQL。缺凭据时返回 None（门禁跳过执行但不假装通过）。"""
    user = os.environ.get("PAIDEIA_TEST_DB_USER")
    password = os.environ.get("PAIDEIA_TEST_DB_PASSWORD")
    if not user or password is None:
        return None
    try:
        import pymysql
        from pymysql.constants import CLIENT
    except ImportError:
        return None
    return pymysql.connect(
        host=os.environ.get("PAIDEIA_TEST_DB_HOST", "127.0.0.1"),
        port=int(os.environ.get("PAIDEIA_TEST_DB_PORT", "3306")),
        user=user, password=password, charset="utf8mb4", autocommit=True,
        client_flag=CLIENT.MULTI_STATEMENTS)


class SqlRunner:
    """在一个一次性库里跑 SQL，返回**所有**结果集（用于 reference 与 antiPattern 对比）。"""

    def __init__(self, connection, database: str):
        self.connection = connection
        self.database = database
        self.cursor = connection.cursor()
        self.cursor.execute(f"DROP DATABASE IF EXISTS `{database}`")
        self.cursor.execute(f"CREATE DATABASE `{database}`")
        self.cursor.execute(f"USE `{database}`")

    def run(self, sql: str) -> list[dict]:
        self.cursor.execute(sql)
        tables: list[dict] = []
        while True:
            if self.cursor.description is not None:
                columns = [column[0] for column in self.cursor.description]
                rows = []
                for row in self.cursor.fetchall():
                    # NULL 要变成判题侧的标记，否则与空串混同
                    rows.append([sql_judge.NULL_MARKER if value is None else str(value)
                                 for value in row])
                tables.append({"columns": columns, "rows": rows})
            if not self.cursor.nextset():
                break
        return tables

    def reset(self, dataset: str) -> None:
        """重建库并灌数据集：反模式改过数据后必须回到干净状态再跑下一条。"""
        self.cursor.execute(f"DROP DATABASE IF EXISTS `{self.database}`")
        self.cursor.execute(f"CREATE DATABASE `{self.database}`")
        self.cursor.execute(f"USE `{self.database}`")
        self.cursor.execute(dataset)

    def close(self) -> None:
        try:
            self.cursor.execute(f"DROP DATABASE IF EXISTS `{self.database}`")
        except Exception:  # noqa: BLE001
            pass
        self.connection.close()


# ── 题目与课程文本 ──────────────────────────────────────────────────────


def load_datasets() -> dict[str, str]:
    datasets = {}
    for path in sorted(glob.glob(str(DATASET_DIR / "*.sql"))):
        datasets[Path(path).stem] = Path(path).read_text(encoding="utf-8")
    return datasets


def load_cases() -> list[dict]:
    cases = []
    for path in sorted(glob.glob(str(CASES_DIR / "*.json"))):
        data = json.loads(Path(path).read_text(encoding="utf-8"))
        if isinstance(data, list):
            cases.extend(data)
        else:
            cases.append(data)
    return cases


def lesson_text_of(course: str) -> str:
    """第 N 课的正文，用于核对 trap 是否对应「常见误区」。"""
    matches = sorted(glob.glob(str(COURSE_DIR / f"{course}-*.md")))
    if not matches:
        return ""
    return Path(matches[0]).read_text(encoding="utf-8")


def misconceptions(body: str) -> list[str]:
    return [match.strip() for match in re.findall(r"^### 误区[：:]\s*(.+)$", body, re.M)]


def _plain(text: str) -> str:
    """比对前去掉 markdown 反引号与空白：标题里的 `NULL` 与题面里的 NULL 是同一个东西，
    要求作者在两个地方敲出完全一样的反引号只会制造无意义的报错。"""
    return re.sub(r"[`\s]", "", text)


# ── 校验 ────────────────────────────────────────────────────────────────


def check_metadata(case: dict) -> None:
    name = case.get("id", "<无 id>")
    for key in ("id", "kind", "title", "goal", "trap", "course"):
        if not case.get(key):
            fail(name, f"缺少 {key}")
    kind = case.get("kind")
    if kind not in ALL_KINDS:
        fail(name, f"kind 只能是 {' / '.join(ALL_KINDS)}")
    if case.get("course"):
        body = lesson_text_of(str(case["course"]))
        if not body:
            fail(name, f"找不到第 {case['course']} 课的正文")
            return
        trap = str(case.get("trap", ""))
        # trap 允许是误区标题的一部分（标题长，题目里写短的便于阅读）
        hits = [item for item in misconceptions(body)
                if trap and (_plain(trap) in _plain(item) or _plain(item) in _plain(trap))]
        if not hits:
            fail(name, f"trap「{trap}」在本课「常见误区」里找不到对应条目——不许出超纲题")


def check_executable(case: dict, runner: SqlRunner | None, datasets: dict[str, str],
                     write_hashes: bool) -> None:
    name = case["id"]
    dataset_keys = case.get("dataset") or []
    if len(dataset_keys) < 2:
        fail(name, "至少要有 2 个数据集（1 公开 + 1 隐藏），否则硬编码答案也能过")
        return
    for key in dataset_keys:
        if key not in datasets:
            fail(name, f"数据集 {key} 不存在")
            return

    reference = case.get("reference")
    if not reference:
        fail(name, "缺少 reference（期望结果必须由参考解真跑得出，不能手写）")
        return
    if not case.get("alternatives"):
        fail(name, "缺少 alternatives：没有它就无法证明判题不误杀等价写法")
        return
    if not case.get("antiPatterns"):
        fail(name, "缺少 antiPatterns：没有它就无法证明这题能区分真假")
        return

    if runner is None:
        # 没有数据库就只查结构，但**明确说明跳过执行**——不能当成通过
        print(f"  ! {name}：没有数据库凭据，跳过执行校验（未验证）", file=sys.stderr)
        return

    normalize = case.get("normalize") or {}
    hashes: dict[str, str] = {}
    for key in dataset_keys:
        dataset = datasets[key]
        runner.reset(dataset)
        expected_sets = runner.run(reference)
        if not expected_sets:
            fail(name, f"数据集 {key}：reference 没有返回任何结果集")
            continue
        expected = expected_sets[0]

        # ① 每个等价写法都必须与 reference 结果相同
        for index, alternative in enumerate(case["alternatives"]):
            runner.reset(dataset)
            got_sets = runner.run(alternative)
            got = got_sets[0] if got_sets else {"columns": [], "rows": []}
            if not sql_judge.judge_sql(expected, got, normalize):
                fail(name, f"数据集 {key}：alternatives[{index}] 被判为不同——"
                           f"说明判题口径太窄，会误杀正确写法")

        # ② 每个反模式必须在**至少一个**数据集上与 reference 不同
        #    这里只记录"是否至少一处不同"，跨数据集的汇总在下面
        for index, anti in enumerate(case["antiPatterns"]):
            runner.reset(dataset)
            got_sets = runner.run(anti)
            got = got_sets[0] if got_sets else {"columns": [], "rows": []}
            if not sql_judge.judge_sql(expected, got, normalize):
                case.setdefault("_anti_caught", {}).setdefault(index, []).append(key)

        hashes[key] = sql_judge.canonical_sha256(expected, normalize)

    # ③ 反模式汇总：每个反模式至少在一个数据集上被抓到
    caught = case.get("_anti_caught", {})
    for index in range(len(case.get("antiPatterns") or [])):
        if index not in caught:
            fail(name, f"antiPatterns[{index}] 在**所有**数据集上都与参考解相同——"
                       f"这题没有区分度，任何写法都能过")

    # ④ 哈希回写（默认只校验，不写盘）
    declared = case.get("expectHash") or {}
    if write_hashes:
        case["expectHash"] = hashes
    elif declared != hashes:
        differ = [key for key in hashes if declared.get(key) != hashes[key]]
        if differ:
            fail(name, f"expectHash 与实跑不一致（{', '.join(differ)}）——"
                       f"数据集或参考解改过？跑 --write-hashes 重算")


def check_readonly(case: dict) -> None:
    """不执行的题型：至少要有一个答案字段。"""
    name = case["id"]
    if not (case.get("options") or case.get("answer") is not None or case.get("template")):
        fail(name, f"{case.get('kind')} 需要 options / answer / template 之一")


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description="SQL 题目质量门禁")
    parser.add_argument("--check", action="store_true", help="校验（默认动作）")
    parser.add_argument("--write-hashes", action="store_true", help="回写 expectHash")
    parser.add_argument("--course", help="只校验某一课（如 03）")
    arguments = parser.parse_args(argv or ["--check"])

    datasets = load_datasets()
    cases = load_cases()
    if arguments.course:
        cases = [case for case in cases if str(case.get("course", "")) == arguments.course]
    if not cases:
        print("没有找到题目——空目录视为通过")
        return 0

    connection = _connect()
    runner = None
    if connection is not None:
        runner = SqlRunner(connection, f"paideia_cases_{os.getpid()}")

    try:
        for case in cases:
            check_metadata(case)
            if case.get("kind") in EXECUTABLE_KINDS:
                check_executable(case, runner, datasets, arguments.write_hashes)
            else:
                check_readonly(case)
    finally:
        if runner is not None:
            runner.close()

    if arguments.write_hashes:
        write_back(cases)

    if failures:
        print(f"\n{len(failures)} 个问题：\n")
        for failure in failures:
            print(" - " + failure)
        return 1
    print(f"通过：{len(cases)} 道题")
    return 0


def write_back(cases: list[dict]) -> None:
    """把算出的哈希写回题目文件（逐个文件按 id 合并，不重排原文件）。"""
    by_id = {case["id"]: case.get("expectHash") for case in cases if "expectHash" in case}
    for path in sorted(glob.glob(str(CASES_DIR / "*.json"))):
        file = Path(path)
        data = json.loads(file.read_text(encoding="utf-8"))
        single = isinstance(data, dict)
        items = [data] if single else data
        changed = False
        for item in items:
            hashes = by_id.get(item.get("id"))
            if hashes and item.get("expectHash") != hashes:
                item["expectHash"] = hashes
                changed = True
        if changed:
            file.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            print(f"已回写 {file.name}")


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
