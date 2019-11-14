package com.atguigu.gmall.item.vo;

import lombok.Data;

/**
 * 基本属性的值和名字
 */
@Data
public class BaseAttrVO {

    private Long attrId; //属性的id
    private String attrName; //属性的name
    private String[] attrValues; //属性值的数组

}
