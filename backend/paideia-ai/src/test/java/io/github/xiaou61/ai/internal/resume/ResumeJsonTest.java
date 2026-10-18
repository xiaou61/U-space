package io.github.xiaou61.ai.internal.resume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 简历 JSON 的严格解析（WORK-018 TASK-003）。
 *
 * <p>三组用例各自对着一类真实故障：模型多给字段（丢弃）、模型编造条目（引证对不上）、
 * 模型把数组写成字符串（当空处理而不是整段失败）。
 */
class ResumeJsonTest {

    private final ResumeJson json = new ResumeJson();

    private static final String SOURCE = """
            张三
            电话 13800000000  邮箱 zhangsan@example.com
            教育经历
            某大学 计算机科学与技术 2019.09 - 2023.06
            工作经历
            某公司 后端开发工程师 2023.07 - 至今
            - 负责订单系统，把接口平均响应从 800ms 降到 200ms
            - 参与支付对账模块的拆分
            技能
            Java、MySQL、Redis
            """;

    @Test
    @DisplayName("正常返回：字段按结构读出，引证能对上的条目保留")
    void parsesWellFormedDraft() {
        String raw = """
                {
                  "basics": {"name":"张三","phone":"13800000000","email":"zhangsan@example.com",
                             "city":"杭州","links":["github.com/zhangsan"]},
                  "intent": {"role":"Java 后端","city":"杭州","availability":"随时"},
                  "education": [{"school":"某大学","degree":"本科","major":"计算机科学与技术",
                                 "start":"2019.09","end":"2023.06",
                                 "quote":"某大学 计算机科学与技术 2019.09 - 2023.06"}],
                  "work": [{"company":"某公司","title":"后端开发工程师","start":"2023.07","end":"至今",
                            "summary":"负责订单系统",
                            "bullets":["把接口平均响应从 800ms 降到 200ms","参与支付对账模块的拆分"],
                            "quote":"某公司 后端开发工程师 2023.07 - 至今"}],
                  "projects": [],
                  "skills": [{"group":"语言","items":["Java"]},{"group":"存储","items":["MySQL","Redis"]}],
                  "awards": [],
                  "selfEvaluation": "喜欢做性能优化"
                }
                """;

        ResumeJson.ParsedDraft draft = json.parseDraft(raw, SOURCE);

        assertThat(draft.dropped()).isZero();
        assertThat(draft.data().basics().name()).isEqualTo("张三");
        assertThat(draft.data().work()).hasSize(1);
        assertThat(draft.data().work().get(0).bullets()).hasSize(2);
        assertThat(draft.data().skills()).hasSize(2);
    }

    @Test
    @DisplayName("引证在原文里找不到：丢掉那条（模型编造经历的最廉价闸门）")
    void dropsEntriesWithUnverifiableQuote() {
        String raw = """
                {
                  "basics": {"name":"张三"},
                  "intent": {},
                  "education": [{"school":"某大学","quote":"某大学 计算机科学与技术 2019.09 - 2023.06"}],
                  "work": [
                    {"company":"某公司","title":"后端开发工程师","quote":"某公司 后端开发工程师 2023.07 - 至今"},
                    {"company":"字节跳动","title":"高级架构师","quote":"字节跳动 高级架构师 2021 - 2023"}
                  ],
                  "projects": [], "skills": [], "awards": [], "selfEvaluation": ""
                }
                """;

        ResumeJson.ParsedDraft draft = json.parseDraft(raw, SOURCE);

        assertThat(draft.data().work()).hasSize(1);
        assertThat(draft.data().work().get(0).company()).isEqualTo("某公司");
        assertThat(draft.dropped()).isEqualTo(1);
    }

    @Test
    @DisplayName("引证为空时不丢：不是每条都能干净地引一句，缺引证不等于编造")
    void keepsEntriesWithoutQuote() {
        String raw = """
                {"basics": {}, "intent": {},
                 "education": [], "work": [{"company":"某公司","title":"后端"}],
                 "projects": [], "skills": [], "awards": [], "selfEvaluation": ""}
                """;

        ResumeJson.ParsedDraft draft = json.parseDraft(raw, SOURCE);

        assertThat(draft.data().work()).hasSize(1);
        assertThat(draft.dropped()).isZero();
    }

    @Test
    @DisplayName("未知字段与错误类型：丢弃/当空，不让整次调用失败")
    void toleratesUnknownFieldsAndWrongTypes() {
        String raw = """
                {"basics": {"name":"张三","nickname":"小张"},
                 "intent": {}, "education": [], "work": [],
                 "projects": [], "skills": [], "awards": [], "selfEvaluation": "",
                 "extraSection": {"foo":"bar"}}
                """;

        ResumeJson.ParsedDraft draft = json.parseDraft(raw, SOURCE);

        assertThat(draft.data().basics().name()).isEqualTo("张三");
        assertThat(draft.data().work()).isEmpty();
    }

    @Test
    @DisplayName("bullets 写成字符串而不是数组：当空数组，不炸")
    void toleratesStringInsteadOfArray() {
        String raw = """
                {"basics": {}, "intent": [], "education": [],
                 "work": [{"company":"某公司","title":"后端","bullets":"做过订单系统"}],
                 "projects": [], "skills": [], "awards": [], "selfEvaluation": ""}
                """;

        ResumeJson.ParsedDraft draft = json.parseDraft(raw, "\n" + SOURCE + "\n");

        assertThat(draft.data().work()).hasSize(1);
        assertThat(draft.data().work().get(0).bullets()).isEmpty();
    }

    @Test
    @DisplayName("超长字段被截断并计数（用户要能看到\"后面没读进去\"）")
    void truncatesOverlongText() {
        String long_ = "字".repeat(ResumeJson.MAX_TEXT + 100);
        String raw = """
                {"basics": {"name":"%s"}, "intent": {}, "education": [], "work": [],
                 "projects": [], "skills": [], "awards": [], "selfEvaluation": ""}
                """.formatted(long_);

        ResumeJson.ParsedDraft draft = json.parseDraft(raw, SOURCE);

        assertThat(draft.data().basics().name()).hasSize(ResumeJson.MAX_TEXT);
        assertThat(draft.truncatedFields()).isEqualTo(1);
    }

    @Test
    @DisplayName("空返回、非 JSON、非对象：一律判失败，绝不落库")
    void rejectsUnusableOutput() {
        assertThatThrownBy(() -> json.parseDraft("", SOURCE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有返回任何内容");
        assertThatThrownBy(() -> json.parseDraft("我觉得你的简历不错", SOURCE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不是合法 JSON");
        assertThatThrownBy(() -> json.parseDraft("[1,2,3]", SOURCE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不是一个 JSON 对象");
    }

    @Test
    @DisplayName("```json 围栏：只在整段被包住时剥掉")
    void stripsFence() {
        String raw = """
                ```json
                {"basics":{"name":"张三"},"intent":{},"education":[],"work":[],
                 "projects":[],"skills":[],"awards":[],"selfEvaluation":""}
                ```
                """;

        assertThat(json.parseDraft(raw, SOURCE).data().basics().name()).isEqualTo("张三");
    }

    @Test
    @DisplayName("写库再读回：结构化数据不变")
    void roundTripsThroughStorage() {
        ResumeData original = new ResumeData(
                new ResumeData.Basics("张三", "13800000000", "a@b.c", "杭州", java.util.List.of("github.com/x")),
                new ResumeData.Intent("Java 后端", "杭州", "随时"),
                java.util.List.of(new ResumeData.Education("某大学", "本科", "计算机", "2019.09", "2023.06",
                        java.util.List.of("绩点 3.8"))),
                java.util.List.of(new ResumeData.Work("某公司", "后端", "2023.07", "至今", "负责订单系统",
                        java.util.List.of("响应从 800ms 到 200ms"))),
                java.util.List.of(new ResumeData.Project("订单系统", "主力开发", "2024", java.util.List.of("拆分服务"))),
                java.util.List.of(new ResumeData.SkillGroup("语言", java.util.List.of("Java"))),
                java.util.List.of(new ResumeData.Award("一等奖学金", "某大学", "2021")),
                "喜欢性能优化");

        ResumeData read = json.parseStored(json.write(original));

        assertThat(read).isEqualTo(original);
    }
}
