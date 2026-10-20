#!/usr/bin/env bash
# 一次性安装/更新运维监控（WORK-028）。在服务器上以 root 运行：
#
#   PAIDEIA_DB_PASSWORD=... GRAFANA_ADMIN_PASSWORD=... GRAFANA_SMTP_PASSWORD=... bash install-ops.sh
#
# **幂等**：可反复跑。已存在的凭据文件不会被覆盖，重装不会把口令换掉。
#
# **必须带代理**：实测 Docker Hub 与 grafana apt 源直连失败、走 127.0.0.1:7890 均成功。
# 不带代理时表现为"下载卡住、无报错"——这是本项最容易卡住的一步。
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OPS_DIR=/opt/paideia-ops
BACKUP_DIR=/opt/paideia-backup
TEXTFILE_DIR=/var/lib/node_exporter/textfile
export http_proxy="${http_proxy:-http://127.0.0.1:7890}"
export https_proxy="${https_proxy:-http://127.0.0.1:7890}"

# 版本固定：latest 会在某天悄悄变行为，而这里没有任何自动升级机制
PROMETHEUS_VERSION=2.54.1
NODE_EXPORTER_VERSION=1.8.2
MYSQLD_EXPORTER_VERSION=0.15.1
REDIS_EXPORTER_VERSION=1.67.0
BLACKBOX_EXPORTER_VERSION=0.25.0

say() { printf '\n=== %s ===\n' "$1"; }

say "目录与服务账号"
mkdir -p "$OPS_DIR" "$BACKUP_DIR" /etc/paideia-backup "$TEXTFILE_DIR" /etc/prometheus /etc/blackbox
mkdir -p /etc/grafana/provisioning/datasources /etc/grafana/provisioning/dashboards /etc/grafana/provisioning/alerting
for account in prometheus node_exporter mysqld_exporter redis_exporter blackbox_exporter; do
    id "$account" >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin "$account"
done
# grafana 包的 postinst 通常会建这个账号；这里兜底，缺了会以 status=217/USER 拒绝启动
id grafana >/dev/null 2>&1 || useradd --system --home /var/lib/grafana --shell /usr/sbin/nologin grafana
chown -R node_exporter:node_exporter /var/lib/node_exporter

say "Grafana（不在 Ubuntu 源里，需加官方源）"
if ! command -v grafana-server >/dev/null 2>&1 && [ ! -x /usr/share/grafana/bin/grafana ]; then
    install -d -m 0755 /etc/apt/keyrings
    curl -fsSL --retry 3 --retry-all-errors \
        https://apt.grafana.com/gpg.key -o /etc/apt/keyrings/grafana.asc
    chmod 0644 /etc/apt/keyrings/grafana.asc
    echo 'deb [signed-by=/etc/apt/keyrings/grafana.asc] https://apt.grafana.com stable main' \
        > /etc/apt/sources.list.d/grafana.list
    apt-get update -qq
    DEBIAN_FRONTEND=noninteractive apt-get install -y grafana
fi
mkdir -p /var/lib/grafana /var/log/grafana
chown -R grafana:grafana /var/lib/grafana /var/log/grafana

say "脚本与配置"
install -m 0755 "$HERE/paideia-backup.sh" "$OPS_DIR/paideia-backup.sh"
install -m 0755 "$HERE/ops-textfile.sh"   "$OPS_DIR/ops-textfile.sh"
install -m 0644 "$HERE/prometheus.yml"    /etc/prometheus/prometheus.yml
install -m 0644 "$HERE/blackbox.yml"      /etc/blackbox/blackbox.yml
install -m 0644 "$HERE/grafana.ini"       /etc/grafana/grafana.ini
cp -r "$HERE/provisioning/." /etc/grafana/provisioning/
chown -R grafana:grafana /etc/grafana/provisioning 2>/dev/null || true

say "凭据文件（已存在则保留）"
# 口令由调用方经环境变量传入：不写进本脚本、不进仓库、不进 journal
if [ ! -f /etc/paideia-backup/client.cnf ]; then
    umask 077
    printf '[client]\nuser=paideia\npassword=%s\nhost=127.0.0.1\n' \
        "${PAIDEIA_DB_PASSWORD:?需要环境变量 PAIDEIA_DB_PASSWORD}" > /etc/paideia-backup/client.cnf
fi
chmod 600 /etc/paideia-backup/client.cnf

if [ ! -f /etc/paideia-backup/exporter.cnf ]; then
    umask 077
    printf '[client]\nuser=paideia\npassword=%s\nhost=127.0.0.1\n' \
        "${PAIDEIA_DB_PASSWORD:?需要环境变量 PAIDEIA_DB_PASSWORD}" > /etc/paideia-backup/exporter.cnf
fi
chmod 600 /etc/paideia-backup/exporter.cnf
chown mysqld_exporter:mysqld_exporter /etc/paideia-backup/exporter.cnf

if [ ! -f /etc/grafana/grafana.env ]; then
    umask 077
    cat > /etc/grafana/grafana.env <<ENVEOF
GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:?需要环境变量 GRAFANA_ADMIN_PASSWORD}
GF_SMTP_ENABLED=true
GF_SMTP_HOST=smtp.qq.com:465
GF_SMTP_USER=${GRAFANA_SMTP_USER:-3153566913@qq.com}
GF_SMTP_PASSWORD=${GRAFANA_SMTP_PASSWORD:?需要环境变量 GRAFANA_SMTP_PASSWORD}
GF_SMTP_FROM_ADDRESS=${GRAFANA_SMTP_USER:-3153566913@qq.com}
GF_SMTP_FROM_NAME=Paideia 运维
ENVEOF
fi
chmod 600 /etc/grafana/grafana.env

# 解压安装一个 github release 里的单个二进制
install_release() {
    local repo=$1 version=$2 name=$3 tar=$4
    local target="/usr/local/bin/$name"
    [ -x "$target" ] && { echo "已有 $target"; return; }
    local url="https://github.com/$repo/releases/download/v${version}/${tar}.tar.gz"
    # --retry-all-errors：经代理时偶发 SSL_ERROR_SYSCALL（实测），一次失败不代表源不可用
    curl -fsSL --retry 3 --retry-all-errors --retry-delay 2 "$url" -o "/tmp/$tar.tgz"
    tar -xzf "/tmp/$tar.tgz" -C /tmp
    install -m 0755 "/tmp/$tar/$name" "$target"
    rm -rf "/tmp/$tar" "/tmp/$tar.tgz"
}

say "下载（走代理）"
install_release prometheus/prometheus "$PROMETHEUS_VERSION" prometheus "prometheus-${PROMETHEUS_VERSION}.linux-amd64"
install_release prometheus/prometheus "$PROMETHEUS_VERSION" promtool "prometheus-${PROMETHEUS_VERSION}.linux-amd64"
install_release prometheus/node_exporter "$NODE_EXPORTER_VERSION" node_exporter "node_exporter-${NODE_EXPORTER_VERSION}.linux-amd64"
install_release prometheus/mysqld_exporter "$MYSQLD_EXPORTER_VERSION" mysqld_exporter "mysqld_exporter-${MYSQLD_EXPORTER_VERSION}.linux-amd64"
install_release prometheus/blackbox_exporter "$BLACKBOX_EXPORTER_VERSION" blackbox_exporter "blackbox_exporter-${BLACKBOX_EXPORTER_VERSION}.linux-amd64"
install_release oliver006/redis_exporter "$REDIS_EXPORTER_VERSION" redis_exporter "redis_exporter-v${REDIS_EXPORTER_VERSION}.linux-amd64"

mkdir -p /var/lib/prometheus
chown -R prometheus:prometheus /var/lib/prometheus

say "安装 systemd 单元"
for unit in prometheus node-exporter mysqld-exporter redis-exporter blackbox-exporter ops-textfile grafana paideia-backup; do
    [ -f "$HERE/$unit.service" ] && install -m 0644 "$HERE/$unit.service" "/etc/systemd/system/$unit.service"
    [ -f "$HERE/$unit.timer" ] && install -m 0644 "$HERE/$unit.timer" "/etc/systemd/system/$unit.timer"
done
systemctl daemon-reload

say "启动"
for unit in prometheus node-exporter mysqld-exporter redis-exporter blackbox-exporter grafana; do
    systemctl enable --now "$unit" || echo "⚠ $unit 启动失败，见 journalctl -u $unit"
done
systemctl enable --now ops-textfile.timer paideia-backup.timer
# 立刻跑一次，不等下一个整分钟
systemctl start ops-textfile.service || true

say "结果"
for unit in prometheus grafana node-exporter mysqld-exporter redis-exporter blackbox-exporter ops-textfile.timer paideia-backup.timer; do
    printf '%-22s %s\n' "$unit" "$(systemctl is-active "$unit" 2>/dev/null || true)"
done
echo '--- 监听端口（应全部是 127.0.0.1）---'
ss -ltn | grep -E ':(9090|9100|9104|9121|9115|3000)\s' || echo "（没有监控端口监听——异常）"

# **每条告警规则必须显式指定收件人**（实测踩过）：Grafana 13 的 provisioned 规则
# 不指定 notification_settings 时，`__grafana_receiver__` 落到 `empty` 收件人——
# 告警照样 firing、照样进历史，但**一封邮件都不发**，日志里只有
# "Sending alerts to local notifier"，看起来一切正常。
missed=$(grep -cE '^\s+- uid: paideia-' /etc/grafana/provisioning/alerting/paideia-rules.yaml || true)
with_receiver=$(grep -c 'receiver: paideia-mail' /etc/grafana/provisioning/alerting/paideia-rules.yaml || true)
if [ "$missed" != "$with_receiver" ]; then
    echo "⚠ 告警规则数（$missed）与指定收件人的规则数（$with_receiver）不一致：有规则不会发信"
fi
