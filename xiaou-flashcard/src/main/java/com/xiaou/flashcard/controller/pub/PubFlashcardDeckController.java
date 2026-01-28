package com.xiaou.flashcard.controller.pub;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.dto.response.FlashcardDeckVO;
import com.xiaou.flashcard.dto.response.FlashcardVO;
import com.xiaou.flashcard.service.FlashcardDeckService;
import com.xiaou.flashcard.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡卡组公开控制器
 *
 * @author xiaou
 */
@Tag(name = "闪卡卡组公开接口")
@RestController
@RequestMapping("/pub/flashcard/deck")
@RequiredArgsConstructor
public class PubFlashcardDeckController {

    private final FlashcardDeckService deckService;
    private final FlashcardService flashcardService;

    @Operation(summary = "获取公开卡组列表")
    @GetMapping("/list")
    public Result<List<FlashcardDeckVO>> getPublicDecks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags) {
        Long userId = null;
        try {
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            // 未登录用户
        }
        List<FlashcardDeckVO> decks = deckService.getPublicDecks(keyword, tags, userId);
        return Result.success(decks);
    }

    @Operation(summary = "获取公开卡组详情")
    @GetMapping("/{id}")
    public Result<FlashcardDeckVO> getPublicDeckById(@PathVariable Long id) {
        Long userId = null;
        try {
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            // 未登录用户
        }
        FlashcardDeckVO deck = deckService.getDeckById(id, userId);
        return Result.success(deck);
    }

    @Operation(summary = "获取公开卡组的闪卡列表")
    @GetMapping("/{id}/cards")
    public Result<List<FlashcardVO>> getPublicDeckCards(@PathVariable Long id) {
        Long userId = null;
        try {
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            // 未登录用户
        }
        List<FlashcardVO> cards = flashcardService.getCardsByDeckId(id, userId);
        return Result.success(cards);
    }
}
