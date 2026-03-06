package com.huacai.assisting.service;

import com.huacai.assisting.domain.ItemSimilarity;
import com.huacai.assisting.mapper.ItemSimilarityMapper;
import com.huacai.assisting.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.util.*;
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

    // 相似度阈值，低于此值的相似度不存储
    private static final double SIMILARITY_THRESHOLD = 0.01;

    // 每个商品最多保留的相似商品数量（控制存储量）
    private static final int MAX_SIMILAR_PER_ITEM = 100;

    /**
     * 项目启动时计算一次，然后每天凌晨2点定时更新
     */
    @PostConstruct
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void computeSimilarities() {
        System.out.println("开始计算商品相似度...");

        // 1. 获取用户-商品交互数据（已完成订单中的购买记录）
        List<Map<String, Object>> interactions = ordersMapper.selectUserProductInteractions("已完成");

        // 2. 构建用户-商品矩阵 (userId -> (productId -> interaction))
        Map<Long, Map<String, Double>> userProductMap = buildUserProductMap(interactions);

        // 3. 构建商品-用户向量 (productId -> (userId -> interaction))
        Map<String, Map<Long, Double>> productUserMap = buildProductUserMap(userProductMap);

        // 4. 获取所有商品ID列表
        List<String> allProductIds = new ArrayList<>(productUserMap.keySet());

        // 5. 准备相似度数据列表（只保留每个商品最相似的TOP N）
        Map<String, List<ItemSimilarity>> topSimilarMap = new HashMap<>();

        for (int i = 0; i < allProductIds.size(); i++) {
            String prodA = allProductIds.get(i);
            Map<Long, Double> vecA = productUserMap.get(prodA);
            if (vecA == null || vecA.isEmpty()) continue;

            for (int j = i + 1; j < allProductIds.size(); j++) {
                String prodB = allProductIds.get(j);
                Map<Long, Double> vecB = productUserMap.get(prodB);
                if (vecB == null || vecB.isEmpty()) continue;

                double similarity = cosineSimilarity(vecA, vecB);
                if (similarity > SIMILARITY_THRESHOLD) {
                    // 分别保存 (A->B) 和 (B->A) 两条记录
                    topSimilarMap.computeIfAbsent(prodA, k -> new ArrayList<>())
                            .add(new ItemSimilarity(prodA, prodB, similarity));
                    topSimilarMap.computeIfAbsent(prodB, k -> new ArrayList<>())
                            .add(new ItemSimilarity(prodB, prodA, similarity));
                }
            }
        }

        // 6. 对每个商品的相似列表进行排序，只保留 TOP N
        List<ItemSimilarity> finalList = new ArrayList<>();
        for (Map.Entry<String, List<ItemSimilarity>> entry : topSimilarMap.entrySet()) {
            List<ItemSimilarity> sorted = entry.getValue().stream()
                    .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                    .limit(MAX_SIMILAR_PER_ITEM)
                    .collect(Collectors.toList());
            finalList.addAll(sorted);
        }

        // 7. 清空旧数据，插入新数据（先删除后插入，保证原子性）
        itemSimilarityMapper.truncateTable();
        if (!finalList.isEmpty()) {
            // 分批插入，避免一次插入过多导致内存溢出
            int batchSize = 2000;
            for (int i = 0; i < finalList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, finalList.size());
                itemSimilarityMapper.batchInsert(finalList.subList(i, end));
            }
        }

        System.out.println("商品相似度计算完成，共生成 " + finalList.size() + " 条相似关系。");
    }

    /**
     * 构建用户-商品交互矩阵
     */
    private Map<Long, Map<String, Double>> buildUserProductMap(List<Map<String, Object>> interactions) {
        Map<Long, Map<String, Double>> map = new HashMap<>();
        for (Map<String, Object> row : interactions) {
            Long userId = ((Number) row.get("user_id")).longValue();
            String productId = (String) row.get("products_id");
            Double interaction = ((Number) row.get("interaction")).doubleValue();

            map.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(productId, interaction);
        }
        return map;
    }

    /**
     * 转置为用户-商品矩阵，得到商品-用户向量
     */
    private Map<String, Map<Long, Double>> buildProductUserMap(Map<Long, Map<String, Double>> userProductMap) {
        Map<String, Map<Long, Double>> map = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : userProductMap.entrySet()) {
            Long userId = entry.getKey();
            for (Map.Entry<String, Double> prodEntry : entry.getValue().entrySet()) {
                String productId = prodEntry.getKey();
                Double interaction = prodEntry.getValue();
                map.computeIfAbsent(productId, k -> new HashMap<>())
                        .put(userId, interaction);
            }
        }
        return map;
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private double cosineSimilarity(Map<Long, Double> vecA, Map<Long, Double> vecB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<Long, Double> entry : vecA.entrySet()) {
            Long userId = entry.getKey();
            Double valA = entry.getValue();
            Double valB = vecB.get(userId);
            if (valB != null) {
                dotProduct += valA * valB;
            }
            normA += valA * valA;
        }
        for (Double valB : vecB.values()) {
            normB += valB * valB;
        }

        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 对外提供：获取与指定商品最相似的N个商品
     */
    public List<String> getTopSimilarItems(String productId, int topN) {
        return itemSimilarityMapper.selectTopSimilar(productId, topN);
    }
}