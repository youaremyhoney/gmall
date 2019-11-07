package com.atguigu.gmall.pms.VO;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuInfoVO extends SpuInfoEntity {

    //图片信息
    private List<String> spuImages;

    //基本属性信息
    private List<ProductAttrValueVO> baseAttrs;

    //sku信息
    private List<SkuInfoVO> skus;

}
