package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

/**
 * spu基本属性的组合
 */
@Data
public class BaseGroupVO {

    private Long id;
    private String name; //分组的名字
    private List<BaseAttrVO> attrs;

}
