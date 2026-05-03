package com.huacai.assisting.service;

import com.huacai.assisting.domain.Products;
import com.huacai.assisting.mapper.CartMapper;
import com.huacai.assisting.mapper.OrdersMapper;
import com.huacai.assisting.mapper.ProductViewLogMapper;
import com.huacai.assisting.mapper.ProductsMapper;
import com.huacai.assisting.vo.RecommendItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品推荐服务
 */
@Service
public class RecommendationService {

    public static final String SCENE_DEFAULT = "default";
    public static final String SCENE_BUY_AGAIN = "buy_again";
    public static final String SCENE_CROSS_SELL = "cross_sell";

    private static final int DEFAULT_HISTORY_FETCH_SIZE = 20;
    private static final int DEFAULT_CART_FETCH_SIZE = 12;
    private static final int DEFAULT_VIEW_FETCH_SIZE = 20;
    private static final int DEFAULT_SIMILAR_FETCH_SIZE = 12;
    private static final int HOT_PRODUCT_FETCH_MULTIPLIER = 4;
    private static final double MIN_SIMILARITY_SCORE = 0.05D;
    private static final double RECENCY_DECAY = 0.88D;
    private static final double CART_BEHAVIOR_WEIGHT = 0.6D;
    private static final double VIEW_BEHAVIOR_WEIGHT = 0.3D;
    private static final double SAME_ORIGIN_BOOST = 0.18D;
    private static final double SAME_PROVINCE_BOOST = 0.1D;
    private static final double SAME_FARMER_BOOST = 0.12D;

    @Autowired
    private ItemSimilarityService similarityService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductViewLogMapper productViewLogMapper;

    @Autowired
    private ProductsMapper productsMapper;

    public List<RecommendItemVo> recommendForScene(Long userId, int topN, String scene, List<String> seedProductIds) {
        String normalizedScene = normalizeScene(scene);
        if (SCENE_BUY_AGAIN.equals(normalizedScene)) {
            return recommendBuyAgain(userId, topN);
        }
        if (SCENE_CROSS_SELL.equals(normalizedScene)) {
            return recommendCrossSell(userId, topN, seedProductIds);
        }
        return recommendForUser(userId, topN);
    }

    public List<RecommendItemVo> recommendForUser(Long userId, int topN) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        List<String> purchaseHistory = normalizeSeedProductIds(
                ordersMapper.selectRecentProductIdsByUserId(userId, DEFAULT_HISTORY_FETCH_SIZE)
        );
        List<String> cartHistory = normalizeSeedProductIds(
                cartMapper.selectProductIdsByUserId(userId, DEFAULT_CART_FETCH_SIZE)
        );
        List<String> viewHistory = normalizeSeedProductIds(
                productViewLogMapper.selectRecentProductIdsByUserId(userId, DEFAULT_VIEW_FETCH_SIZE)
        );

        if (purchaseHistory.isEmpty() && cartHistory.isEmpty() && viewHistory.isEmpty()) {
            return buildRecommendItems(getHotProducts(topN, new LinkedHashSet<>()), "平台当前热销商品");
        }

        Set<String> excludedProductIds = new LinkedHashSet<>();
        excludedProductIds.addAll(purchaseHistory);
        excludedProductIds.addAll(cartHistory);
        excludedProductIds.addAll(viewHistory);

        Map<String, Double> candidateScores = new HashMap<>();
        Map<String, String> reasonMap = new HashMap<>();

        if (!purchaseHistory.isEmpty()) {
            Map<String, Double> purchaseScores = scoreBySimilarItems(purchaseHistory, excludedProductIds, 1.0D);
            mergeCandidateScores(candidateScores, purchaseScores);
            mergeReasons(reasonMap, buildDefaultReasonMap(purchaseScores.keySet(), purchaseHistory));
        }

        if (!cartHistory.isEmpty()) {
            Map<String, Double> cartScores = scoreBySimilarItems(cartHistory, excludedProductIds, CART_BEHAVIOR_WEIGHT);
            mergeCandidateScores(candidateScores, cartScores);
            mergeReasons(reasonMap, buildCartReasonMap(cartScores.keySet(), cartHistory));
        }

        if (!viewHistory.isEmpty()) {
            Map<String, Double> viewScores = scoreBySimilarItems(viewHistory, excludedProductIds, VIEW_BEHAVIOR_WEIGHT);
            mergeCandidateScores(candidateScores, viewScores);
            mergeReasons(reasonMap, buildViewReasonMap(viewScores.keySet(), viewHistory));
        }

        if (!candidateScores.isEmpty()) {
            applyBusinessBoosts(candidateScores, mergeReferenceProducts(purchaseHistory, cartHistory, viewHistory), reasonMap);
        }

        List<String> recommendations = sortAndCollect(candidateScores, topN);
        if (recommendations.size() < topN) {
            excludedProductIds.addAll(recommendations);
            List<String> hotProducts = getHotProducts(topN - recommendations.size(), excludedProductIds);
            recommendations.addAll(hotProducts);
            hotProducts.forEach(productId -> reasonMap.put(productId, "平台当前热销商品"));
        }

        if (recommendations.size() < topN) {
            excludedProductIds.addAll(recommendations);
            List<String> latestProducts = getLatestProducts(topN - recommendations.size(), excludedProductIds);
            recommendations.addAll(latestProducts);
            latestProducts.forEach(productId -> reasonMap.put(productId, "为你补充推荐平台上新商品"));
        }

        return buildRecommendItems(
                recommendations.stream().distinct().limit(topN).collect(Collectors.toList()),
                reasonMap
        );
    }

    public List<RecommendItemVo> recommendBuyAgain(Long userId, int topN) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> repurchaseRows = ordersMapper.selectRepurchaseCandidatesByUserId(userId, topN * 3);
        if (repurchaseRows == null || repurchaseRows.isEmpty()) {
            return buildRecommendItems(getHotProducts(topN, new LinkedHashSet<>()), "平台当前热销商品");
        }

        Map<String, Double> repurchaseScores = new HashMap<>();
        Map<String, String> reasonMap = new HashMap<>();
        for (Map<String, Object> row : repurchaseRows) {
            String productId = String.valueOf(row.get("products_id"));
            double orderCount = toDouble(row.get("order_count"));
            double quantity = toDouble(row.get("total_quantity"));
            double recencyScore = calculateRecencyScore(row.get("last_purchase_time"));
            double score = orderCount * 1.4D + quantity * 0.35D + recencyScore;
            repurchaseScores.put(productId, score);
            reasonMap.put(productId, orderCount >= 2 ? "你曾多次购买过这类商品" : "你近期购买过该商品");
        }

        return buildRecommendItems(sortAndCollect(repurchaseScores, topN), reasonMap);
    }

    public List<RecommendItemVo> recommendCrossSell(Long userId, int topN, List<String> seedProductIds) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        List<String> normalizedSeeds = normalizeSeedProductIds(seedProductIds);
        if (normalizedSeeds.isEmpty()) {
            return recommendForUser(userId, topN);
        }

        Set<String> excludedProductIds = new LinkedHashSet<>(normalizedSeeds);
        Map<String, Double> candidateScores = scoreBySimilarItems(normalizedSeeds, excludedProductIds, 1.0D);
        Map<String, String> reasonMap = buildCrossSellReasonMap(candidateScores.keySet(), normalizedSeeds);

        if (!candidateScores.isEmpty()) {
            applyBusinessBoosts(candidateScores, normalizedSeeds, reasonMap);
        }

        List<String> recommendations = sortAndCollect(candidateScores, topN);
        if (recommendations.size() < topN) {
            excludedProductIds.addAll(recommendations);
            List<String> hotProducts = getHotProducts(topN - recommendations.size(), excludedProductIds);
            recommendations.addAll(hotProducts);
            hotProducts.forEach(productId -> reasonMap.put(productId, "适合作为购物车商品的补充选择"));
        }

        return buildRecommendItems(
                recommendations.stream().distinct().limit(topN).collect(Collectors.toList()),
                reasonMap
        );
    }

    private Map<String, Double> scoreBySimilarItems(List<String> sourceProductIds, Set<String> excludedProductIds, double behaviorWeight) {
        Map<String, Double> candidateScores = new HashMap<>();

        for (int index = 0; index < sourceProductIds.size(); index++) {
            String sourceProductId = sourceProductIds.get(index);
            double recencyWeight = Math.pow(RECENCY_DECAY, index);
            List<Map<String, Object>> similarItems =
                    similarityService.getTopSimilarItemsWithScores(sourceProductId, DEFAULT_SIMILAR_FETCH_SIZE);

            for (Map<String, Object> similarItem : similarItems) {
                String similarProductId = toStringValue(similarItem, "item_id_b", "itemIdB", "ITEM_ID_B");
                double similarityScore = toDoubleValue(similarItem, "similarity", "similarityScore", "SIMILARITY");

                if (!hasText(similarProductId) || similarityScore < MIN_SIMILARITY_SCORE || excludedProductIds.contains(similarProductId)) {
                    continue;
                }

                candidateScores.merge(similarProductId, similarityScore * recencyWeight * behaviorWeight, Double::sum);
            }
        }

        return candidateScores;
    }

    private Map<String, Double> scoreBySimilarItems(List<String> sourceProductIds, Set<String> excludedProductIds) {
        return scoreBySimilarItems(sourceProductIds, excludedProductIds, 1.0D);
    }

    private Map<String, String> buildDefaultReasonMap(Set<String> candidateIds, List<String> historyProductIds) {
        Map<String, String> reasonMap = new HashMap<>();
        if (candidateIds.isEmpty() || historyProductIds.isEmpty()) {
            return reasonMap;
        }

        List<Products> historyProducts = productsMapper.selectProductsListByIds(historyProductIds);
        List<Products> candidateProducts = productsMapper.selectProductsListByIds(new ArrayList<>(candidateIds));
        if (candidateProducts == null || candidateProducts.isEmpty()) {
            return reasonMap;
        }

        Set<String> preferredOrigins = historyProducts.stream()
                .map(Products::getOrigin)
                .filter(this::hasText)
                .collect(Collectors.toSet());
        Set<String> preferredProvinces = historyProducts.stream()
                .map(Products::getOrigin)
                .map(this::extractProvince)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        Set<Long> preferredFarmerIds = historyProducts.stream()
                .map(Products::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Products product : candidateProducts) {
            if (preferredOrigins.contains(product.getOrigin())) {
                reasonMap.put(product.getProductsId(), "与你近期购买的同产地商品相关");
            } else if (preferredProvinces.contains(extractProvince(product.getOrigin()))) {
                reasonMap.put(product.getProductsId(), "与你近期关注的同省助农商品相关");
            } else if (product.getUserId() != null && preferredFarmerIds.contains(product.getUserId())) {
                reasonMap.put(product.getProductsId(), "来自你偏好的助农农户");
            } else {
                reasonMap.put(product.getProductsId(), "与你近期购买商品相似度较高");
            }
        }
        return reasonMap;
    }

    private Map<String, String> buildCartReasonMap(Set<String> candidateIds, List<String> cartProductIds) {
        Map<String, String> reasonMap = new HashMap<>();
        if (candidateIds.isEmpty() || cartProductIds.isEmpty()) {
            return reasonMap;
        }

        List<Products> cartProducts = productsMapper.selectProductsListByIds(cartProductIds);
        List<Products> candidateProducts = productsMapper.selectProductsListByIds(new ArrayList<>(candidateIds));
        if (candidateProducts == null || candidateProducts.isEmpty()) {
            return reasonMap;
        }

        Set<String> cartOrigins = cartProducts.stream()
                .map(Products::getOrigin)
                .filter(this::hasText)
                .collect(Collectors.toSet());
        Set<String> cartProvinces = cartProducts.stream()
                .map(Products::getOrigin)
                .map(this::extractProvince)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        for (Products product : candidateProducts) {
            if (cartOrigins.contains(product.getOrigin())) {
                reasonMap.put(product.getProductsId(), "与你购物车中的同产地商品相关");
            } else if (cartProvinces.contains(extractProvince(product.getOrigin()))) {
                reasonMap.put(product.getProductsId(), "与你购物车中的同省助农商品相关");
            } else {
                reasonMap.put(product.getProductsId(), "与你加入购物车的商品兴趣接近");
            }
        }
        return reasonMap;
    }

    private Map<String, String> buildViewReasonMap(Set<String> candidateIds, List<String> viewProductIds) {
        Map<String, String> reasonMap = new HashMap<>();
        if (candidateIds.isEmpty() || viewProductIds.isEmpty()) {
            return reasonMap;
        }

        List<Products> viewedProducts = productsMapper.selectProductsListByIds(viewProductIds);
        List<Products> candidateProducts = productsMapper.selectProductsListByIds(new ArrayList<>(candidateIds));
        if (candidateProducts == null || candidateProducts.isEmpty()) {
            return reasonMap;
        }

        Set<String> viewedOrigins = viewedProducts.stream()
                .map(Products::getOrigin)
                .filter(this::hasText)
                .collect(Collectors.toSet());
        Set<String> viewedProvinces = viewedProducts.stream()
                .map(Products::getOrigin)
                .map(this::extractProvince)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        for (Products product : candidateProducts) {
            if (viewedOrigins.contains(product.getOrigin())) {
                reasonMap.put(product.getProductsId(), "与你最近浏览的同产地商品相关");
            } else if (viewedProvinces.contains(extractProvince(product.getOrigin()))) {
                reasonMap.put(product.getProductsId(), "与你最近浏览的同省助农商品相关");
            } else {
                reasonMap.put(product.getProductsId(), "与你最近浏览的商品兴趣接近");
            }
        }
        return reasonMap;
    }

    private Map<String, String> buildCrossSellReasonMap(Set<String> candidateIds, List<String> seedProductIds) {
        Map<String, String> reasonMap = new HashMap<>();
        if (candidateIds.isEmpty() || seedProductIds.isEmpty()) {
            return reasonMap;
        }

        List<Products> seedProducts = productsMapper.selectProductsListByIds(seedProductIds);
        List<Products> candidateProducts = productsMapper.selectProductsListByIds(new ArrayList<>(candidateIds));
        if (candidateProducts == null || candidateProducts.isEmpty()) {
            return reasonMap;
        }

        Set<String> seedOrigins = seedProducts.stream()
                .map(Products::getOrigin)
                .filter(this::hasText)
                .collect(Collectors.toSet());
        Set<String> seedProvinces = seedProducts.stream()
                .map(Products::getOrigin)
                .map(this::extractProvince)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        for (Products product : candidateProducts) {
            if (seedOrigins.contains(product.getOrigin())) {
                reasonMap.put(product.getProductsId(), "可与购物车中的同产地商品搭配购买");
            } else if (seedProvinces.contains(extractProvince(product.getOrigin()))) {
                reasonMap.put(product.getProductsId(), "可与购物车中的同省助农商品搭配购买");
            } else {
                reasonMap.put(product.getProductsId(), "与你购物车中的商品搭配度较高");
            }
        }
        return reasonMap;
    }

    private void applyBusinessBoosts(Map<String, Double> candidateScores, List<String> referenceProductIds, Map<String, String> reasonMap) {
        List<Products> referenceProducts = productsMapper.selectProductsListByIds(referenceProductIds);
        if (referenceProducts == null || referenceProducts.isEmpty()) {
            return;
        }

        Set<String> preferredOrigins = referenceProducts.stream()
                .map(Products::getOrigin)
                .filter(this::hasText)
                .collect(Collectors.toSet());
        Set<String> preferredProvinces = referenceProducts.stream()
                .map(Products::getOrigin)
                .map(this::extractProvince)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        Set<Long> preferredFarmerIds = referenceProducts.stream()
                .map(Products::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (preferredOrigins.isEmpty() && preferredProvinces.isEmpty() && preferredFarmerIds.isEmpty()) {
            return;
        }

        List<String> candidateIds = new ArrayList<>(candidateScores.keySet());
        List<Products> candidateProducts = productsMapper.selectProductsListByIds(candidateIds);
        if (candidateProducts == null || candidateProducts.isEmpty()) {
            return;
        }

        for (Products product : candidateProducts) {
            double boost = 0D;
            if (preferredOrigins.contains(product.getOrigin())) {
                boost += SAME_ORIGIN_BOOST;
                reasonMap.put(product.getProductsId(), "优先展示同产地助农商品");
            } else if (preferredProvinces.contains(extractProvince(product.getOrigin()))) {
                boost += SAME_PROVINCE_BOOST;
                reasonMap.put(product.getProductsId(), "优先展示同省助农商品");
            }
            if (product.getUserId() != null && preferredFarmerIds.contains(product.getUserId())) {
                boost += SAME_FARMER_BOOST;
                reasonMap.put(product.getProductsId(), "优先展示你偏好的农户商品");
            }
            if (boost > 0D) {
                final double finalBoost = boost;
                candidateScores.computeIfPresent(product.getProductsId(), (key, score) -> score + finalBoost);
            }
        }
    }

    private void mergeCandidateScores(Map<String, Double> targetScores, Map<String, Double> sourceScores) {
        sourceScores.forEach((productId, score) -> targetScores.merge(productId, score, Double::sum));
    }

    private void mergeReasons(Map<String, String> targetReasons, Map<String, String> sourceReasons) {
        sourceReasons.forEach(targetReasons::putIfAbsent);
    }

    private List<String> mergeReferenceProducts(List<String> purchaseHistory, List<String> cartHistory, List<String> viewHistory) {
        LinkedHashSet<String> mergedProducts = new LinkedHashSet<>();
        mergedProducts.addAll(purchaseHistory);
        mergedProducts.addAll(cartHistory);
        mergedProducts.addAll(viewHistory);
        return new ArrayList<>(mergedProducts);
    }

    private List<String> sortAndCollect(Map<String, Double> candidateScores, int topN) {
        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> getHotProducts(int topN, Set<String> excludedProductIds) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        int fetchSize = Math.max(topN * HOT_PRODUCT_FETCH_MULTIPLIER, topN);
        List<String> hotProducts = ordersMapper.selectHotProductIds(fetchSize);
        if (hotProducts == null || hotProducts.isEmpty()) {
            return new ArrayList<>();
        }

        return hotProducts.stream()
                .filter(productId -> !excludedProductIds.contains(productId))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private List<String> getLatestProducts(int topN, Set<String> excludedProductIds) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        List<String> latestProducts = productsMapper.selectLatestProductIdsExclude(new ArrayList<>(excludedProductIds), topN);
        if (latestProducts == null || latestProducts.isEmpty()) {
            return new ArrayList<>();
        }
        return latestProducts.stream()
                .filter(productId -> !excludedProductIds.contains(productId))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private List<String> normalizeSeedProductIds(List<String> seedProductIds) {
        if (seedProductIds == null || seedProductIds.isEmpty()) {
            return new ArrayList<>();
        }
        return seedProductIds.stream()
                .filter(this::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeScene(String scene) {
        return hasText(scene) ? scene.trim().toLowerCase() : SCENE_DEFAULT;
    }

    private double calculateRecencyScore(Object timeValue) {
        Instant lastPurchaseInstant = toInstant(timeValue);
        if (lastPurchaseInstant == null) {
            return 0D;
        }
        long days = ChronoUnit.DAYS.between(lastPurchaseInstant, Instant.now());
        if (days <= 7) {
            return 1.2D;
        }
        if (days <= 30) {
            return 0.8D;
        }
        if (days <= 90) {
            return 0.4D;
        }
        return 0.1D;
    }

    private Instant toInstant(Object timeValue) {
        if (timeValue instanceof Timestamp) {
            return ((Timestamp) timeValue).toInstant();
        }
        if (timeValue instanceof java.util.Date) {
            return ((java.util.Date) timeValue).toInstant();
        }
        return null;
    }

    private List<RecommendItemVo> buildRecommendItems(List<String> productIds, String defaultReason) {
        return productIds.stream()
                .map(productId -> new RecommendItemVo(productId, defaultReason))
                .collect(Collectors.toList());
    }

    private List<RecommendItemVo> buildRecommendItems(List<String> productIds, Map<String, String> reasonMap) {
        return productIds.stream()
                .map(productId -> new RecommendItemVo(productId, reasonMap.getOrDefault(productId, "为你智能匹配的助农商品")))
                .collect(Collectors.toList());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String extractProvince(String origin) {
        if (!hasText(origin)) {
            return null;
        }
        String normalizedOrigin = origin.trim();
        String[] suffixes = {"省", "市", "自治区", "壮族自治区", "回族自治区", "维吾尔自治区", "特别行政区"};
        for (String suffix : suffixes) {
            int index = normalizedOrigin.indexOf(suffix);
            if (index > 0) {
                return normalizedOrigin.substring(0, index + suffix.length());
            }
        }
        if (normalizedOrigin.length() >= 2) {
            return normalizedOrigin.substring(0, 2);
        }
        return normalizedOrigin;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0D;
    }

    private String toStringValue(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private double toDoubleValue(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) {
            return 0D;
        }
        for (String key : keys) {
            Object value = data.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value != null) {
                try {
                    return Double.parseDouble(String.valueOf(value));
                } catch (NumberFormatException ignored) {
                    // continue trying the next key
                }
            }
        }
        return 0D;
    }
}
