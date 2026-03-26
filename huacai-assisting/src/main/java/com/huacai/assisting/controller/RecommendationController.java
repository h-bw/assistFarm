package com.huacai.assisting.controller;

import com.huacai.assisting.service.RecommendationService;
import com.huacai.assisting.vo.RecommendItemVo;
import com.huacai.common.core.domain.AjaxResult;
import com.huacai.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品推荐控制器
 */
@RestController
@RequestMapping("/assisting/recommend")
public class RecommendationController {
    private static final int MAX_SEED_PRODUCT_IDS = 20;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取用户的个性化推荐商品ID列表
     * @param userId 用户ID
     * @param topN 推荐数量（默认10）
     * @return 商品ID列表
     */
    @GetMapping("/user/{userId}")
    public AjaxResult getUserRecommendations(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "10") Integer topN,
                                             @RequestParam(required = false) String scene,
                                             @RequestParam(required = false) String productIds) {
        Long loginUserId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(loginUserId) && !loginUserId.equals(userId)) {
            return AjaxResult.error("无权查看其他用户的推荐结果");
        }
        List<String> seedProductIds = parseProductIds(productIds);
        List<RecommendItemVo> recommendationItems = recommendationService.recommendForScene(userId, topN, scene, seedProductIds);
        return AjaxResult.success(recommendationItems);
    }

    private List<String> parseProductIds(String productIds) {
        if (productIds == null || productIds.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return Arrays.stream(productIds.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .limit(MAX_SEED_PRODUCT_IDS)
                .collect(Collectors.toList());
    }

    /**
     * 手动触发相似度计算（仅管理员可用）
     */
    @PostMapping("/compute")
    public AjaxResult computeSimilarities() {
        // 由于 @Scheduled 会自动执行，此方法仅为手动触发调试，可以注入 ItemSimilarityService 调用 computeSimilarities()
        // 实际使用时建议加上权限控制 @PreAuthorize("@ss.hasPermi('system:admin')")
        return AjaxResult.success("相似度计算已触发，请稍后查看结果");
    }
}
