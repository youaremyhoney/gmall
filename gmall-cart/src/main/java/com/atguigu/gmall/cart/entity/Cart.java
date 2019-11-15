package com.atguigu.gmall.cart.entity;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private Long skuId; // 商品的skuId

    private boolean check; // 是否选中

    private String title; // 标题

    private String defaultImage; //默认图片

    private BigDecimal price; //加入购物车时候的价格

    private Integer count; //加入购物车的数量

    private List<SkuSaleAttrValueEntity> skuAttrValue; //sku的销售属性

    private List<ItemSaleVO> sales; // 所有的优惠信息

}
