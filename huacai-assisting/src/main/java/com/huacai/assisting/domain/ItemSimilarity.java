package com.huacai.assisting.domain;

/**
 * 商品相似度实体对象（对应数据库表 item_similarity）
 */
public class ItemSimilarity {

    /** 商品A ID */
    private String itemIdA;

    /** 商品B ID */
    private String itemIdB;

    /** 相似度 */
    private Double similarity;

    // 无参构造方法
    public ItemSimilarity() {
    }

    // 全参构造方法（便于快速创建对象）
    public ItemSimilarity(String itemIdA, String itemIdB, Double similarity) {
        this.itemIdA = itemIdA;
        this.itemIdB = itemIdB;
        this.similarity = similarity;
    }

    // Getter 和 Setter 方法
    public String getItemIdA() {
        return itemIdA;
    }

    public void setItemIdA(String itemIdA) {
        this.itemIdA = itemIdA;
    }

    public String getItemIdB() {
        return itemIdB;
    }

    public void setItemIdB(String itemIdB) {
        this.itemIdB = itemIdB;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return "ItemSimilarity{" +
                "itemIdA='" + itemIdA + '\'' +
                ", itemIdB='" + itemIdB + '\'' +
                ", similarity=" + similarity +
                '}';
    }
}