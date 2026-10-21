-- 订单系统公开数据集（WORK-032）。
--
-- 每道 SQL 题都在这个数据集上执行。**只放 DECIMAL，不放 FLOAT/DOUBLE**：
-- 浮点误差会让判题不稳定，门禁会检查并在出现时直接报错。
--
-- 这份是**公开**的：随题面下发给学习者，他们能看到表结构与数据。

CREATE TABLE customers (
  id   INT PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  city VARCHAR(20) NOT NULL
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
  (1, 'ann', '北京'),
  (2, 'bob', '上海'),
  (3, 'cid', '北京');

INSERT INTO orders VALUES
  (10, 1, '2026-10-20 10:00:00'),
  (11, 2, '2026-10-21 09:00:00'),
  (12, 1, '2026-10-21 15:00:00');

INSERT INTO order_items VALUES
  (100, 10, '键盘',   199.00, 2),
  (101, 10, '鼠标',    89.50, 1),
  (102, 11, '显示器', 1099.00, 1),
  (103, 12, '鼠标',    89.50, 3);
