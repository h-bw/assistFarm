package com.huacai.assisting.domain;

import java.math.BigDecimal;
import com.huacai.common.annotation.Excel;
import lombok.*;
import com.huacai.common.core.domain.BaseEntity;

/**
 * 农户产品对象 products
 *
 * @author huacai
 * @date 2025-08-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Products extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 产品ID */
    private String productsId;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String name;

    /** 简介 */
    @Excel(name = "简介")
    private String subtitle;

    /** 价格 */
    @Excel(name = "价格")
    private BigDecimal price;

    /** 图片 */
    @Excel(name = "图片")
    private String image;

    /** 产地 */
    @Excel(name = "产地")
    private String origin;

    /** 最低价格（查询用） */
    private BigDecimal minPrice;

    /** 最高价格（查询用） */
    private BigDecimal maxPrice;

    /** 发货地 */
    @Excel(name = "发货地")
    private String shipFrom;

    /** 库存 */
    @Excel(name = "库存")
    private BigDecimal inventory;

    /** 规格 */
    @Excel(name = "规格")
    private String specs;

    /** 保质期 */
    @Excel(name = "保质期")
    private String expire;

    /** 储存方法 */
    @Excel(name = "储存方法")
    private String storage;

    /** 食用方法 */
    @Excel(name = "食用方法")
    private String edible;

    /** 详情 */
    @Excel(name = "详情")
    private String detail;

    /** 类目（用于电商化展示） */
    private String category;

    /** 累计销量（用于电商化展示） */
    private Long sales;

    /** 好评率（百分比，例：98.6） */
    private BigDecimal rating;

    /** 评价数 */
    private Long reviewCount;

    /**
     * 图集（多图），JSON 字符串或逗号分隔 URL 列表均可（前端做兼容解析）
     * 例：["https://.../1.jpg","https://.../2.jpg"]
     */
    private String galleryImages;

    /** 发货承诺（如：48小时发货/冷链配送） */
    private String shippingPromise;

    /** 服务标签（如：坏果包赔,7天无理由），逗号分隔 */
    private String serviceTags;

    /** 农户认证标识（1已认证，0未认证） */
    private Integer farmerVerified;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    // 创建人(农户)用户名
    private String userName;


}
