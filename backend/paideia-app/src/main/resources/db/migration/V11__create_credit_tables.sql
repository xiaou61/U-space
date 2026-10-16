-- AI 积分账户与流水（WORK-009 T-05）。
--
-- 两张表分开的理由：**余额要能原子扣**（一条带条件的 UPDATE，见 CreditService.reserve），
-- **流水要只增不改**（审计要的是"发生过什么"）。合成一张表两件事都做不好。
--
-- 与 V4/V5/V6 一致：**不建跨模块外键**，级联删除由应用层按清单负责（见 AiUserDeletionListener）。
-- 注销时**两张表都整行删除**（用户决定 8：审计一并删，守住 WORK-006 裁决 10 的硬删承诺）。

CREATE TABLE ai_credit_accounts (
  user_id    BIGINT UNSIGNED NOT NULL,
  -- 积分，整数。不用小数：避免浮点误差，也避免出现"余额 3.999 分"这种界面。
  -- 不加 CHECK 约束：真要出问题，宁可让账面看得见，也不要让写入被数据库挡成 500。
  -- "不会为负"由扣减 SQL 的 `balance >= ?` 条件保证（那才是并发的真正防线）。
  balance    BIGINT          NOT NULL DEFAULT 0,
  updated_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id)
) ENGINE = InnoDB;

-- 流水自带计量量，**不靠 join 消息表**：
-- 精简那一路的用量本来就没有落库的地方（`ai_entry_summaries` 只存卡片与模型），
-- 如果计费记录要从别处 join 出来，那一半消耗就永远查不到。计费记录自己带量，两路才一致。
CREATE TABLE ai_credit_ledger (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED NOT NULL,
  -- 正 = 入账，负 = 出账。
  delta            BIGINT          NOT NULL,
  -- ADJUST（管理员调整）| RESERVE（预留）| SETTLE（结算差额）| REFUND（退还）
  kind             VARCHAR(16)     NOT NULL,
  -- 这次计量的量；RESERVE / REFUND 为 NULL（那几步没有用量）。
  model            VARCHAR(64)     NULL,
  input_tokens     INT             NULL,
  output_tokens    INT             NULL,
  -- 消耗类的流水指向那条助手消息，供"逐条可对账"（AC-006）；精简那一路为 NULL。
  ref_message_id   BIGINT UNSIGNED NULL,
  -- 管理员调整时才有；审计页的"操作人"列来自它。
  operator_user_id BIGINT UNSIGNED NULL,
  note             VARCHAR(200)    NULL,
  created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_credit_ledger_user (user_id, created_at),
  KEY idx_ai_credit_ledger_message (ref_message_id)
) ENGINE = InnoDB;
