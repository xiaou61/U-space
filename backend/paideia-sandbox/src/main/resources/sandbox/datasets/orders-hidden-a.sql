-- 订单系统隐藏数据集 A（WORK-032）。
--
-- **不随题面下发**：学习者只拿到这个数据集的 key，拿不到内容与期望结果。
-- 存在的作用是让「把答案硬编码进去」的写法失败——公开集上的总额是 487.50，
-- 这里刻意让它不是任何一个"看起来眼熟"的数字。
--
-- 与公开集的三处差异（都是刻意的）：
--   1. 行数不同（多一个客户、多两笔订单）
--   2. 有 NULL 订单明细（公开集没有）
--   3. 金额带非整除的小数（避免"凑整数"蒙对）

CREATE TABLE customers (
  id   INT PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  city VARCHAR(20) NOT NULL,
  note VARCHAR(20) NULL
);
CREATE TABLE orders (
  id          INT PRIMARY KEY,
  customer_id INT NOT NULL,
  created     DATETIME NOT NULL
);
CREATE TABLE order_items (
  id         INT PRIMARY KEY,
  order_id   INT NOT NULL,
  product    VARCHAR(20) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  qty        INT NOT NULL
);

INSERT INTO customers VALUES
  (1, 'dan',  '广州', NULL),
  (2, 'eve',  '广州', '老客户'),
  (3, 'fang', '深圳', ''),
  (4, 'guo',  '深圳', NULL);

INSERT INTO orders VALUES
  (20, 1, '2026-10-01 08:00:00'),
  (21, 1, '2026-10-02 09:30:00'),
  (22, 2, '2026-10-03 11:15:00'),
  (23, 4, '2026-10-04 20:45:00');

INSERT INTO order_items VALUES
  (200, 20, '键盘',   199.00, 1),
  (201, 20, '数据线',  19.90, 3),
  (202, 21, '耳机',   299.00, 1),
  (203, 22, '鼠标',    89.50, 2),
  (204, 22, '显示器', 1099.00, 1),
  (205, 23, '键盘',   199.00, 4);
