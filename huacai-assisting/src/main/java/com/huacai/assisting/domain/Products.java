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

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    // 创建人(农户)用户名
    private String userName;


}
