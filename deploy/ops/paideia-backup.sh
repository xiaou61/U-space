#!/usr/bin/env bash
# 每日全量备份（WORK-028 REQ-019..022）。
#
# 三条纪律：
#   1. **凭据走 600 权限的 defaults-extra-file，绝不进命令行**：写进命令行会出现在 `ps` 里，
#      也会被 systemd 记进 journal。
#   2. **先写临时文件再 mv**：dump 中途失败时不能留下一个"看起来完整"的 gzip——
#      那会让"有备份"变成谎言，而发现它的时机恰好是你最需要它的时候。
#   3. **失败要留下痕迹**：last-status.json 会被 ops-textfile 脚本读成指标，
#      于是"上次备份是三天前"可以是一条告警，而不是必须有人打开页面才发现。
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/opt/paideia-backup}"
DATABASE="${DATABASE:-paideia}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"
DEFAULTS_FILE="${DEFAULTS_FILE:-/etc/paideia-backup/client.cnf}"
STATUS_FILE="$BACKUP_DIR/last-status.json"

mkdir -p "$BACKUP_DIR"

started=$(date +%s)
stamp=$(date +%Y%m%d-%H%M%S)
target="$BACKUP_DIR/paideia-$stamp.sql.gz"
temporary="$target.partial"
error_log="$BACKUP_DIR/.dump-error.log"

json_escape() {
    printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g' | tr '\n' ' '
}

write_status() {
    local success=$1 bytes=$2 error=$3
    local duration=$(( $(date +%s) - started ))
    printf '{"success":%s,"at":"%s","durationMs":%s,"bytes":%s,"error":"%s"}\n' \
        "$success" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$((duration * 1000))" "$bytes" \
        "$(json_escape "$error")" > "$STATUS_FILE.tmp"
    mv "$STATUS_FILE.tmp" "$STATUS_FILE"
}

fail() {
    local message=$1
    rm -f "$temporary"
    write_status false 0 "$message"
    echo "备份失败：$message" >&2
    exit 1
}

# dump 的 stderr 单独收：既进状态文件（可诊断），又不会因为管道失败而无迹可寻
if ! mysqldump --defaults-extra-file="$DEFAULTS_FILE" \
        --single-transaction --quick --routines --triggers --events \
        --databases "$DATABASE" 2>"$error_log" | gzip -9 > "$temporary"; then
    message=$(head -c 500 "$error_log" 2>/dev/null | tail -1)
    fail "${message:-mysqldump 执行失败}"
fi

# 空文件或坏 gzip 都算失败：宁可不产出，也不要把一个不能用的文件当成功
[ -s "$temporary" ] || fail "dump 产物为空"
gzip -t "$temporary" 2>/dev/null || fail "gzip 校验不通过"

mv "$temporary" "$target"
rm -f "$error_log"

# 滚动删除：只删本脚本命名规则的旧文件，不碰目录里的任何别的东西
find "$BACKUP_DIR" -maxdepth 1 -name 'paideia-*.sql.gz' -type f -mtime "+$RETENTION_DAYS" -delete

bytes=$(stat -c %s "$target")
write_status true "$bytes" ""
echo "备份完成：$target（$bytes 字节）"
