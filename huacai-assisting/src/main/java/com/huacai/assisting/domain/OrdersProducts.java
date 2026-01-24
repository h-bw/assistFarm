package com.huacai.assisting.domain;

import java.math.BigDecimal;
import com.huacai.common.annotation.Excel;
import lombok.*;
import com.huacai.common.core.domain.BaseEntity;
/**
 * 订单产品对象 orders_products
 *
 * @author huacai
 * @date 2025-08-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersProducts extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单产品ID */
    private Long opId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String ordersId;

    /** 产品ID */
    @Excel(name = "产品ID")
    private String productsId;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productsName;

    /** 产品图片 */
    @Excel(name = "产品图片")
    private String image;

    /** 规格 */
    @Excel(name = "规格")
    private String specs;

    /** 产地 */
    @Excel(name = "产地")
    private String origin;

    /** 价格 */
    @Excel(name = "价格")
    private BigDecimal price;

    /** 数量 */
    @Excel(name = "数量")
    private Long quantity;


}
