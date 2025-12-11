package com.xiaou.flashcard.controller.admin;

import com.xiaou.common.core.domain.Result;
import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.dto.request.CreateDeckRequest;
import com.xiaou.flashcard.dto.request.GenerateCardRequest;
import com.xiaou.flashcard.service.FlashcardDeckService;
import com.xiaou.flashcard.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡管理员控制器
 *
 * @author xiaou
 */
@Tag(name = "闪卡管理-后台", description = "闪卡后台管理接口")
@RestController
@RequestMapping("/admin/flashcard")
@RequiredArgsConstructor
public class AdminFlashcardController {

    private final FlashcardDeckService deckService;
    private final FlashcardService flashcardService;

    @Operation(summary = "获取官方卡组列表")
    @GetMapping("/decks")
    public Result<List<FlashcardDeck>> getOfficialDecks() {
        List<FlashcardDeck> decks = deckService.getOfficialDecks();
        return Result.success(decks);
    }

    @Operation(summary = "创建官方卡组")
    @PostMapping("/decks")
    public Result<Long> createOfficialDeck(@RequestBody CreateDeckRequest request) {
        // userId为0表示系统卡组
        Long deckId = deckService.createDeck(0L, request);
        return Result.success(deckId);
    }

    @Operation(summary = "从题库分类批量生成官方闪卡")
    @PostMapping("/generate/category")
    public Result<Integer> generateFromCategory(@RequestBody GenerateCardRequest request) {
        // userId为0表示系统创建
        // 支持questionIds批量生成
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            int count = flashcardService.generateFromQuestionIds(0L, request);
            return Result.success(count);
        }
        int count = flashcardService.generateFromCategory(0L, request);
        return Result.success(count);
    }

    @Operation(summary = "获取所有公开卡组")
    @GetMapping("/decks/public")
    public Result<List<FlashcardDeck>> getPublicDecks() {
        List<FlashcardDeck> decks = deckService.getPublicDecks();
        return Result.success(decks);
    }

    @Operation(summary = "获取卡组详情")
    @GetMapping("/decks/{deckId}")
    public Result<FlashcardDeck> getDeckDetail(@PathVariable Long deckId) {
        FlashcardDeck deck = deckService.getDeckById(deckId);
        return Result.success(deck);
    }

    @Operation(summary = "更新卡组")
    @PutMapping("/decks/{deckId}")
    public Result<Void> updateDeck(@PathVariable Long deckId, @RequestBody CreateDeckRequest request) {
        deckService.updateDeck(0L, deckId, request);
        return Result.success();
    }

    @Operation(summary = "删除卡组")
    @DeleteMapping("/decks/{deckId}")
    public Result<Void> deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(0L, deckId);
        return Result.success();
    }

    @Operation(summary = "获取卡组下的闪卡列表")
    @GetMapping("/decks/{deckId}/cards")
    public Result<List<com.xiaou.flashcard.domain.Flashcard>> getCardsByDeck(@PathVariable Long deckId) {
        List<com.xiaou.flashcard.domain.Flashcard> cards = flashcardService.getCardsByDeckId(deckId);
        return Result.success(cards);
    }

    @Operation(summary = "创建闪卡")
    @PostMapping("/cards")
    public Result<Long> createCard(@RequestBody com.xiaou.flashcard.dto.request.CreateCardRequest request) {
        Long cardId = flashcardService.createCard(0L, request);
        return Result.success(cardId);
    }

    @Operation(summary = "更新闪卡")
    @PutMapping("/cards/{cardId}")
    public Result<Void> updateCard(@PathVariable Long cardId, @RequestBody com.xiaou.flashcard.dto.request.CreateCardRequest request) {
        flashcardService.updateCard(0L, cardId, request);
        return Result.success();
    }

    @Operation(summary = "删除闪卡")
    @DeleteMapping("/cards/{cardId}")
    public Result<Void> deleteCard(@PathVariable Long cardId) {
        flashcardService.deleteCard(0L, cardId);
        return Result.success();
    }
}
