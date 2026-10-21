#!/usr/bin/env bash
# 构建预初始化的 MySQL 沙箱镜像（WORK-032 TASK-001）。
#
# 用法：
#   bash backend/paideia-sandbox/images/mysql/build.sh              # 构建并打印 digest
#
# 构建完成后把输出里的 sha256 填进 application.yml 的 paideia.sandbox.sql-image。
set -euo pipefail
TAG="${TAG:-paideia-sandbox-mysql:8.0}"

cd "$(dirname "$0")"
docker build -t "$TAG" .

echo
echo "已构建 $TAG"
echo -n "digest: "
docker inspect --format='{{index .RepoDigests 0}}' "$TAG" 2>/dev/null \
  || docker inspect --format='sha256:{{.Id}}' "$TAG"
echo
echo "启动耗时自检："
START=$(date +%s%N)
docker run --rm --network=none --entrypoint sh "$TAG" -c 'mkdir -p /tmp/nofiles && chown mysql:mysql /tmp/nofiles
         # 镜像里的 datadir 是 a-w 的（防误写），拷贝时 cp -a 会连权限一起保留，
         # 所以拷完必须显式恢复可写——否则 mysqld 报 ibdata1 must be writable
         cp -a /opt/datadir-base /tmp/data && chmod -R u+w /tmp/data && chown -R mysql:mysql /tmp/data
         mysqld --skip-networking --socket=/tmp/m.sock --datadir=/tmp/data \
                --user=mysql --secure-file-priv=/tmp/nofiles --local-infile=0 \
                --skip-log-bin >/tmp/err.log 2>&1 &
         for i in $(seq 1 600); do
           mysqladmin --socket=/tmp/m.sock -uroot ping >/dev/null 2>&1 && break
           sleep 0.02
         done
         mysql --socket=/tmp/m.sock -uroot -e "SELECT VERSION() AS v"' 2>&1 | tail -3
END=$(date +%s%N)
echo "启动到可查询：$(( (END - START) / 1000000 )) ms（基线 4.4–4.7 s）"
