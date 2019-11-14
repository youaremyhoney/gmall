package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 封装商品详情页的vo对象类
 */
@Data
public class ItemVO {

    //1.当前sku的基本属性
    private Long skuId;
    private Long spuId;
    private Long catalogId;         //分类信息
    private Long brandId;           //品牌Id
    private String skuTitle;        //sku标题信息
    private String skuSubtitle;     //sku副标题信息
    private BigDecimal price;       //sku商品的价格
    private BigDecimal weight;      //sku商品的重量

    //2.sku所有的图片属性信息
    private List<String> pics;

    //3.sku所有促销信息
    private List<SaleVo> sales;

    //4.sku所有销售属性的组合
    private List<SkuSaleAttrValueEntity> saleAttrs;

    //5.spu所有基本属性的组合
    private List<BaseGroupVO> attrGroups;

    //6.详情介绍
    private SpuInfoEntity desc;
}
