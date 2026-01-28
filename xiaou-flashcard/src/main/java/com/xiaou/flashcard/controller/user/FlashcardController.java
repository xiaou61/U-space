package com.xiaou.flashcard.controller.user;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.dto.request.FlashcardBatchCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardImportRequest;
import com.xiaou.flashcard.dto.response.FlashcardVO;
import com.xiaou.flashcard.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡控制器（用户端）
 *
 * @author xiaou
 */
@Tag(name = "闪卡管理")
@RestController
@RequestMapping("/flashcard/card")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @Operation(summary = "创建闪卡")
    @PostMapping
    public Result<Long> createCard(@Valid @RequestBody FlashcardCreateRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Long cardId = flashcardService.createCard(request, userId);
        return Result.success(cardId);
    }

    @Operation(summary = "批量创建闪卡")
    @PostMapping("/batch")
    public Result<Integer> batchCreateCards(@Valid @RequestBody FlashcardBatchCreateRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        int count = flashcardService.batchCreateCards(request, userId);
        return Result.success(count);
    }

    @Operation(summary = "更新闪卡")
    @PutMapping("/{id}")
    public Result<Void> updateCard(@PathVariable Long id, @Valid @RequestBody FlashcardCreateRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        flashcardService.updateCard(id, request, userId);
        return Result.success();
    }

    @Operation(summary = "删除闪卡")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCard(@PathVariable Long id) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        flashcardService.deleteCard(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取卡组内的闪卡列表")
    @GetMapping("/deck/{deckId}")
    public Result<List<FlashcardVO>> getCardsByDeckId(@PathVariable Long deckId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        List<FlashcardVO> cards = flashcardService.getCardsByDeckId(deckId, userId);
        return Result.success(cards);
    }

    @Operation(summary = "从面试题库导入闪卡")
    @PostMapping("/import")
    public Result<Integer> importFromQuestionBank(@Valid @RequestBody FlashcardImportRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        int count = flashcardService.importFromQuestionBank(request, userId);
        return Result.success(count);
    }
}
