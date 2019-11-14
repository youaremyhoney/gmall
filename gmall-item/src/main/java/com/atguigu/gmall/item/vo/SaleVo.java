package com.atguigu.gmall.item.vo;

import lombok.Data;

/**
 * 所有优惠信息
 */
@Data
public class SaleVo {

    //0-优惠券  1-满减  2-阶梯
    private Integer type;

    //促销优惠，优惠券的名字
    private String name;

}
