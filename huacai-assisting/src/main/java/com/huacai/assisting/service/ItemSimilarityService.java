package com.huacai.assisting.service;

import com.huacai.assisting.domain.ItemSimilarity;
import com.huacai.assisting.mapper.ItemSimilarityMapper;
import com.huacai.assisting.mapper.OrdersMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品相似度计算服务（基于物品的协同过滤）
 */
@Service
public class ItemSimilarityService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ItemSimilarityMapper itemSimilarityMapper;

    private static final double SIMILARITY_THRESHOLD = 0.01;
    private static final int MAX_SIMILAR_PER_ITEM = 100;

    /**
     * 项目启动时计算一次，之后每天凌晨 2 点增量重算
     */
    @PostConstruct
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void computeSimilarities() {
        System.out.println("开始计算商品相似度...");

        // 推荐建模尽量覆盖真实成交与下单意图，避免受订单状态文案编码影响。
        List<Map<String, Object>> interactions = ordersMapper.selectUserProductInteractions(null);
        Map<Long, Map<String, Double>> userProductMap = buildUserProductMap(interactions);
        Map<String, Map<Long, Double>> productUserMap = buildProductUserMap(userProductMap);
        List<String> allProductIds = new ArrayList<>(productUserMap.keySet());
        Map<String, List<ItemSimilarity>> topSimilarMap = new HashMap<>();

        for (int i = 0; i < allProductIds.size(); i++) {
            String productA = allProductIds.get(i);
            Map<Long, Double> vectorA = productUserMap.get(productA);
            if (vectorA == null || vectorA.isEmpty()) {
                continue;
            }

            for (int j = i + 1; j < allProductIds.size(); j++) {
                String productB = allProductIds.get(j);
                Map<Long, Double> vectorB = productUserMap.get(productB);
                if (vectorB == null || vectorB.isEmpty()) {
                    continue;
                }

                double similarity = cosineSimilarity(vectorA, vectorB);
                if (similarity > SIMILARITY_THRESHOLD) {
                    topSimilarMap.computeIfAbsent(productA, key -> new ArrayList<>())
                            .add(new ItemSimilarity(productA, productB, similarity));
                    topSimilarMap.computeIfAbsent(productB, key -> new ArrayList<>())
                            .add(new ItemSimilarity(productB, productA, similarity));
                }
            }
        }

        List<ItemSimilarity> finalList = new ArrayList<>();
        for (Map.Entry<String, List<ItemSimilarity>> entry : topSimilarMap.entrySet()) {
            List<ItemSimilarity> sorted = entry.getValue().stream()
                    .sorted((left, right) -> Double.compare(right.getSimilarity(), left.getSimilarity()))
                    .limit(MAX_SIMILAR_PER_ITEM)
                    .collect(Collectors.toList());
            finalList.addAll(sorted);
        }

        itemSimilarityMapper.truncateTable();
        if (!finalList.isEmpty()) {
            int batchSize = 2000;
            for (int i = 0; i < finalList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, finalList.size());
                itemSimilarityMapper.batchInsert(finalList.subList(i, end));
            }
        }

        System.out.println("商品相似度计算完成，共生成 " + finalList.size() + " 条相似关系。");
    }

    private Map<Long, Map<String, Double>> buildUserProductMap(List<Map<String, Object>> interactions) {
        Map<Long, Map<String, Double>> map = new HashMap<>();
        for (Map<String, Object> row : interactions) {
            Long userId = ((Number) row.get("user_id")).longValue();
            String productId = String.valueOf(row.get("products_id"));
            Double interaction = ((Number) row.get("interaction")).doubleValue();

            map.computeIfAbsent(userId, key -> new HashMap<>())
                    .put(productId, interaction);
        }
        return map;
    }

    private Map<String, Map<Long, Double>> buildProductUserMap(Map<Long, Map<String, Double>> userProductMap) {
        Map<String, Map<Long, Double>> map = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : userProductMap.entrySet()) {
            Long userId = entry.getKey();
            for (Map.Entry<String, Double> productEntry : entry.getValue().entrySet()) {
                String productId = productEntry.getKey();
                Double interaction = productEntry.getValue();
                map.computeIfAbsent(productId, key -> new HashMap<>())
                        .put(userId, interaction);
            }
        }
        return map;
    }

    private double cosineSimilarity(Map<Long, Double> vectorA, Map<Long, Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<Long, Double> entry : vectorA.entrySet()) {
            Long userId = entry.getKey();
            Double valueA = entry.getValue();
            Double valueB = vectorB.get(userId);
            if (valueB != null) {
                dotProduct += valueA * valueB;
            }
            normA += valueA * valueA;
        }

        for (Double valueB : vectorB.values()) {
            normB += valueB * valueB;
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public List<String> getTopSimilarItems(String productId, int topN) {
        return itemSimilarityMapper.selectTopSimilar(productId, topN);
    }

    public List<Map<String, Object>> getTopSimilarItemsWithScores(String productId, int topN) {
        return itemSimilarityMapper.selectTopSimilarWithScores(productId, topN);
    }
}
