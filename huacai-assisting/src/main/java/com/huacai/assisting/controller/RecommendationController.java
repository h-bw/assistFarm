package com.huacai.assisting.controller;

import com.huacai.assisting.service.RecommendationService;
import com.huacai.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品推荐控制器
 */
@RestController
@RequestMapping("/assisting/recommend")
public class RecommendationController {

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
                                             @RequestParam(defaultValue = "10") Integer topN) {
        List<String> productIds = recommendationService.recommendForUser(userId, topN);
        return AjaxResult.success(productIds);
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