package com.xiaou.ai.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.xiaou.ai.dto.sql.SqlAnalyzeResult;
import com.xiaou.ai.service.AiSqlOptimizeService;
import com.xiaou.ai.util.CozeResponseParser;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.enums.CozeWorkflowEnum;
import com.xiaou.common.utils.CozeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL优化AI服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSqlOptimizeServiceImpl implements AiSqlOptimizeService {

    private final CozeUtils cozeUtils;

    @Override
    public SqlAnalyzeResult analyzeSql(String sql, String explainResult, String explainFormat,
                                        String tableStructures, String mysqlVersion) {
        try {
            if (!cozeUtils.isClientAvailable()) {
                log.warn("Coze客户端不可用，使用降级分析");
                return SqlAnalyzeResult.fallbackResult(explainResult);
            }

            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("sql", sql);
            params.put("explainResult", explainResult);
            params.put("explainFormat", explainFormat != null ? explainFormat : "TABLE");
            params.put("tableStructures", tableStructures);
            params.put("mysqlVersion", mysqlVersion != null ? mysqlVersion : "8.0");

            // 调用工作流
            Result<String> result = cozeUtils.runWorkflow(
                    CozeWorkflowEnum.SQL_OPTIMIZE_ANALYZE, params);

            if (!result.isSuccess() || CozeResponseParser.isErrorResponse(result.getData())) {
                log.warn("Coze工作流调用失败: {}", result.getMessage());
                return SqlAnalyzeResult.fallbackResult(explainResult);
            }

            // 解析结果
            return parseAnalyzeResult(result.getData(), explainResult);

        } catch (Exception e) {
            log.error("SQL分析失败", e);
            return SqlAnalyzeResult.fallbackResult(explainResult);
        }
    }

    /**
     * 解析分析结果
     */
    private SqlAnalyzeResult parseAnalyzeResult(String response, String explainResult) {
        JSONObject json = CozeResponseParser.parse(response);
        if (json == null) {
            return SqlAnalyzeResult.fallbackResult(explainResult);
        }

        SqlAnalyzeResult result = new SqlAnalyzeResult();
        result.setScore(CozeResponseParser.getInt(json, "score", 50));
        result.setOptimizedSql(json.getStr("optimizedSql"));
        result.setKnowledgePoints(json.getBeanList("knowledgePoints", String.class));

        // 解析问题列表
        List<SqlAnalyzeResult.Problem> problems = new ArrayList<>();
        JSONArray problemsArray = json.getJSONArray("problems");
        if (problemsArray != null) {
            for (int i = 0; i < problemsArray.size(); i++) {
                JSONObject item = problemsArray.getJSONObject(i);
                problems.add(new SqlAnalyzeResult.Problem()
                        .setType(item.getStr("type"))
                        .setSeverity(item.getStr("severity"))
                        .setDescription(item.getStr("description"))
                        .setLocation(item.getStr("location")));
            }
        }
        result.setProblems(problems);

        // 解析EXPLAIN分析
        List<SqlAnalyzeResult.ExplainAnalysis> explainAnalysis = new ArrayList<>();
        JSONArray explainArray = json.getJSONArray("explainAnalysis");
        if (explainArray != null) {
            for (int i = 0; i < explainArray.size(); i++) {
                JSONObject item = explainArray.getJSONObject(i);
                explainAnalysis.add(new SqlAnalyzeResult.ExplainAnalysis()
                        .setTable(item.getStr("table"))
                        .setType(item.getStr("type"))
                        .setTypeExplain(item.getStr("typeExplain"))
                        .setKey(item.getStr("key"))
                        .setKeyExplain(item.getStr("keyExplain"))
                        .setRows(item.getInt("rows"))
                        .setExtra(item.getStr("extra"))
                        .setExtraExplain(item.getStr("extraExplain"))
                        .setIssue(item.getStr("issue")));
            }
        }
        result.setExplainAnalysis(explainAnalysis);

        // 解析优化建议
        List<SqlAnalyzeResult.Suggestion> suggestions = new ArrayList<>();
        JSONArray suggestionsArray = json.getJSONArray("suggestions");
        if (suggestionsArray != null) {
            for (int i = 0; i < suggestionsArray.size(); i++) {
                JSONObject item = suggestionsArray.getJSONObject(i);
                suggestions.add(new SqlAnalyzeResult.Suggestion()
                        .setType(item.getStr("type"))
                        .setTitle(item.getStr("title"))
                        .setDdl(item.getStr("ddl"))
                        .setSql(item.getStr("sql"))
                        .setReason(item.getStr("reason"))
                        .setExpectedImprovement(item.getStr("expectedImprovement")));
            }
        }
        result.setSuggestions(suggestions);

        result.setFallback(false);
        return result;
    }
}
