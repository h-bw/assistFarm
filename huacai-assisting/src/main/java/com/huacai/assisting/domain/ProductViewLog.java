package com.huacai.assisting.domain;

import com.huacai.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 商品详情浏览记录对象 product_view_log
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 浏览记录ID */
    private String viewId;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private String productsId;

    /** 浏览来源 */
    private String viewSource;

    /** 浏览次数 */
    private Integer viewCount;

    /** 最近浏览时间 */
    private Date lastViewTime;
}
