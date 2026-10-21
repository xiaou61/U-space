# 预初始化 datadir 的 MySQL 镜像（WORK-032 TASK-001）。
#
# 为什么必须在构建期初始化：实测 `mysqld --initialize` 要 **17.89 秒**，而一次练习的
# SQL 本身往往只跑几十毫秒——运行时初始化会让用户 95% 的时间花在等数据库起来。
# 预初始化后启动到可查询实测 4.4–4.7 秒（server: 4 核 / 15G）。
#
# 镜像里**不含任何用户数据**：只有系统库（mysql / sys / performance_schema / information_schema）。
# 题目数据集由运行时的容器脚本灌入，且每次运行都拷一份干净的 datadir 副本——
# 实测直接复用同一 datadir 会让上一次建的库残留（`CREATE DATABASE` 报 1007）。
#
# 基础镜像按 digest 钉住：`repo:tag` 会漂移，而漂移意味着隔离参数可能配到一个不同的基础镜像。
FROM mysql:8.0@sha256:7dcddc01f13bab2f15cde676d44d01f61fc9f99fe7785e86196dfc07d358ae2b

# datadir 放 /opt 而不是 /var/lib/mysql：后者在容器里常被覆盖成卷，容易误挂。
# 运行时会 `cp -a /opt/datadir-base /tmp/data` 再指向副本，所以这个目录必须只读安全。
RUN mkdir -p /opt/datadir-base \
    && chown mysql:mysql /opt/datadir-base \
    && mysqld --initialize-insecure --datadir=/opt/datadir-base --user=mysql \
    && chmod -R a-w /opt/datadir-base

# 构建期自检：初始化失败必须让构建红，而不是让运行时第一次请求才发现。
RUN test -d /opt/datadir-base/mysql && test -f /opt/datadir-base/ibdata1 \
    && du -sh /opt/datadir-base
