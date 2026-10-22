-- 删除 `ai_model_credentials`（V4 建）。这张表随 WORK-005 的「用户自带 Key（BYOK）」而生，
-- 那条路已被 WORK-009 撤销：上游凭据改由平台在部署配置里持有，用户侧不再有 Key，
-- 读写它的 `CredentialCipher` / `ModelCredential` 也已从代码里删除。表因此变成纯死表——
-- 全仓除了注销级联清单与几个测试的清理语句，没有任何一处引用它。
--
-- 为什么可以安全删：里面的 `api_key_cipher` 是用已删除的主密钥加密的密文，
-- 主密钥既然不在代码里了，这些行再也解不开，留着只有一种作用——让后来读 schema 的人
-- 以为凭据还存数据库里，然后照着它写代码。
DROP TABLE IF EXISTS ai_model_credentials;
