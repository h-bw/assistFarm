package com.huacai.assisting.vo;

/**
 * 推荐结果视图对象
 */
public class RecommendItemVo {

    private String productsId;
    private String reason;

    public RecommendItemVo() {
    }

    public RecommendItemVo(String productsId, String reason) {
        this.productsId = productsId;
        this.reason = reason;
    }

    public String getProductsId() {
        return productsId;
    }

    public void setProductsId(String productsId) {
        this.productsId = productsId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
