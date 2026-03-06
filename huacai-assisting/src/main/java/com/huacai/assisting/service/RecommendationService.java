package com.huacai.assisting.service;

import com.huacai.assisting.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品推荐服务
 */
@Service
public class RecommendationService {

    @Autowired
    private ItemSimilarityService similarityService;

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 为用户生成推荐商品ID列表
     * @param userId 用户ID
     * @param topN 需要推荐的数量
     * @return 商品ID列表
     */
    public List<String> recommendForUser(Long userId, int topN) {
        // 1. 获取用户最近购买过的商品（最多20个）
        List<String> userHistory = ordersMapper.selectRecentProductIdsByUserId(userId, 20);
        if (userHistory.isEmpty()) {
            // 冷启动：返回热门商品
            return getHotProducts(topN);
        }

        // 2. 收集候选商品及其得分
        Map<String, Double> candidateScores = new HashMap<>();
        for (String boughtProduct : userHistory) {
            List<String> similarItems = similarityService.getTopSimilarItems(boughtProduct, 10);
            for (String similar : similarItems) {
                if (userHistory.contains(similar)) continue; // 排除已购买
                candidateScores.merge(similar, 1.0, Double::sum); // 简单累加得分
            }
        }

        // 3. 按得分排序取前N
        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门商品（按总销量）
     */
    private List<String> getHotProducts(int topN) {
        return ordersMapper.selectHotProductIds(topN);
    }
}