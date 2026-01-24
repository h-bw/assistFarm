package com.huacai.assisting.domain;

import com.huacai.common.annotation.Excel;
import lombok.*;
import com.huacai.common.core.domain.BaseEntity;

/**
 * 收货地址对象 addresses
 *
 * @author huacai
 * @date 2025-08-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Addresses extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收货地址ID */
    private String addressesId;

    /** 收货人 */
    @Excel(name = "收货人")
    private String name;

    /** 手机号码 */
    @Excel(name = "手机号码")
    private String phone;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String detail;

    /** 是否默认地址 */
    @Excel(name = "是否默认地址")
    private Boolean isDefault;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    //创建人
    private String userName;


}
