package com.huacai.assisting.domain;

import com.huacai.common.annotation.Excel;
import lombok.*;
import com.huacai.common.core.domain.BaseEntity;

/**
 * 轮播图对象 banner
 *
 * @author huacai
 * @date 2025-08-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Banner extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 轮播图ID */
    private String bannerId;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 描述 */
    @Excel(name = "描述")
    private String description;

    /** 图片 */
    @Excel(name = "图片")
    private String image;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;


}
