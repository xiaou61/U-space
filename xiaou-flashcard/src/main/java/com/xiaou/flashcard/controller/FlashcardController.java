package com.xiaou.flashcard.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.domain.Flashcard;
import com.xiaou.flashcard.dto.request.CreateCardRequest;
import com.xiaou.flashcard.dto.request.GenerateCardRequest;
import com.xiaou.flashcard.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡控制器
 *
 * @author xiaou
 */
@Tag(name = "闪卡管理", description = "闪卡CRUD接口")
@RestController
@RequestMapping("/flashcard/cards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @Operation(summary = "获取卡组的闪卡列表")
    @GetMapping("/deck/{deckId}")
    public Result<List<Flashcard>> getCardsByDeck(@PathVariable Long deckId) {
        List<Flashcard> cards = flashcardService.getCardsByDeckId(deckId);
        return Result.success(cards);
    }

    @Operation(summary = "获取闪卡详情")
    @GetMapping("/{cardId}")
    public Result<Flashcard> getCard(@PathVariable Long cardId) {
        Flashcard card = flashcardService.getCardById(cardId);
        return Result.success(card);
    }

    @Operation(summary = "创建闪卡")
    @PostMapping
    public Result<Long> createCard(@RequestBody CreateCardRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Long cardId = flashcardService.createCard(userId, request);
        return Result.success(cardId);
    }

    @Operation(summary = "从面试题生成闪卡")
    @PostMapping("/generate")
    public Result<Integer> generateFromQuestion(@RequestBody GenerateCardRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        // 支持批量生成
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            int count = flashcardService.generateFromQuestionIds(userId, request);
            return Result.success(count);
        }
        // 单个生成
        flashcardService.generateFromQuestion(userId, request);
        return Result.success(1);
    }

    @Operation(summary = "从分类批量生成闪卡")
    @PostMapping("/generate/batch")
    public Result<Integer> generateFromCategory(@RequestBody GenerateCardRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        int count = flashcardService.generateFromCategory(userId, request);
        return Result.success(count);
    }

    @Operation(summary = "更新闪卡")
    @PutMapping("/{cardId}")
    public Result<Void> updateCard(@PathVariable Long cardId, @RequestBody CreateCardRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        flashcardService.updateCard(userId, cardId, request);
        return Result.success();
    }

    @Operation(summary = "删除闪卡")
    @DeleteMapping("/{cardId}")
    public Result<Void> deleteCard(@PathVariable Long cardId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        flashcardService.deleteCard(userId, cardId);
        return Result.success();
    }

    @Operation(summary = "检查是否已从面试题生成闪卡")
    @GetMapping("/check/{questionId}")
    public Result<Boolean> checkGenerated(@PathVariable Long questionId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        boolean generated = flashcardService.hasGeneratedFromQuestion(userId, questionId);
        return Result.success(generated);
    }
}
