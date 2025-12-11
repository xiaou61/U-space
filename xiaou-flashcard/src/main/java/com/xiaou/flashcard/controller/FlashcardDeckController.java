package com.xiaou.flashcard.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.dto.request.CreateDeckRequest;
import com.xiaou.flashcard.service.FlashcardDeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 闪卡卡组控制器
 *
 * @author xiaou
 */
@Tag(name = "闪卡卡组", description = "闪卡卡组管理接口")
@RestController
@RequestMapping("/flashcard/decks")
@RequiredArgsConstructor
public class FlashcardDeckController {

    private final FlashcardDeckService deckService;

    @Operation(summary = "获取卡组列表(带学习进度)")
    @GetMapping
    public Result<Map<String, List<FlashcardDeck>>> getDecks() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        
        // 获取并填充学习进度
        List<FlashcardDeck> officialDecks = deckService.getOfficialDecksWithProgress(userId);
        List<FlashcardDeck> personalDecks = deckService.getUserDecksWithProgress(userId);
        
        Map<String, List<FlashcardDeck>> result = new HashMap<>();
        result.put("official", officialDecks);
        result.put("personal", personalDecks);
        
        return Result.success(result);
    }

    @Operation(summary = "获取卡组列表(含学习进度)")
    @GetMapping("/with-progress")
    public Result<List<FlashcardDeck>> getDecksWithProgress() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        List<FlashcardDeck> decks = deckService.getUserDecksWithProgress(userId);
        return Result.success(decks);
    }

    @Operation(summary = "获取卡组详情")
    @GetMapping("/{deckId}")
    public Result<FlashcardDeck> getDeck(@PathVariable Long deckId) {
        FlashcardDeck deck = deckService.getDeckById(deckId);
        return Result.success(deck);
    }

    @Operation(summary = "创建卡组")
    @PostMapping
    public Result<Long> createDeck(@RequestBody CreateDeckRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Long deckId = deckService.createDeck(userId, request);
        return Result.success(deckId);
    }

    @Operation(summary = "更新卡组")
    @PutMapping("/{deckId}")
    public Result<Void> updateDeck(@PathVariable Long deckId, @RequestBody CreateDeckRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        deckService.updateDeck(userId, deckId, request);
        return Result.success();
    }

    @Operation(summary = "删除卡组")
    @DeleteMapping("/{deckId}")
    public Result<Void> deleteDeck(@PathVariable Long deckId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        deckService.deleteDeck(userId, deckId);
        return Result.success();
    }
}
