package com.xiaou.flashcard.controller.user;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.dto.request.FlashcardDeckCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardDeckUpdateRequest;
import com.xiaou.flashcard.dto.response.FlashcardDeckVO;
import com.xiaou.flashcard.service.FlashcardDeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡卡组控制器（用户端）
 *
 * @author xiaou
 */
@Tag(name = "闪卡卡组管理")
@RestController
@RequestMapping("/flashcard/deck")
@RequiredArgsConstructor
public class FlashcardDeckController {

    private final FlashcardDeckService deckService;

    @Operation(summary = "创建卡组")
    @PostMapping
    public Result<Long> createDeck(@Valid @RequestBody FlashcardDeckCreateRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Long deckId = deckService.createDeck(request, userId);
        return Result.success(deckId);
    }

    @Operation(summary = "更新卡组")
    @PutMapping
    public Result<Void> updateDeck(@Valid @RequestBody FlashcardDeckUpdateRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        deckService.updateDeck(request, userId);
        return Result.success();
    }

    @Operation(summary = "删除卡组")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDeck(@PathVariable Long id) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        deckService.deleteDeck(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取卡组详情")
    @GetMapping("/{id}")
    public Result<FlashcardDeckVO> getDeckById(@PathVariable Long id) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        FlashcardDeckVO deck = deckService.getDeckById(id, userId);
        return Result.success(deck);
    }

    @Operation(summary = "获取我的卡组列表")
    @GetMapping("/my")
    public Result<List<FlashcardDeckVO>> getMyDecks() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        List<FlashcardDeckVO> decks = deckService.getMyDecks(userId);
        return Result.success(decks);
    }

    @Operation(summary = "复制卡组")
    @PostMapping("/{id}/fork")
    public Result<Long> forkDeck(@PathVariable Long id) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Long newDeckId = deckService.forkDeck(id, userId);
        return Result.success(newDeckId);
    }
}
