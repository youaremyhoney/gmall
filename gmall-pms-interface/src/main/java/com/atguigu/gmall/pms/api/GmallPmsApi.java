package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    //根据父类等级，或者根据父类的id来查询分类集合
    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategory(@RequestParam(value="level", defaultValue = "0")Integer level
            , @RequestParam(value="parentCid", required = false)Long parentCid);

    //查询spu分页信息
    @PostMapping("pms/spuinfo/list")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition);

    //根据spuId查询sku的详细信息
    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId")Long spuId);

    //根据品牌Id查询品牌相关的信息
    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    //根据父分类的Id查询所有匹配的分类信息
    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    //根据spuId查询所有spu属性信息
    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<SpuAttributeValueVO>> querySearchAttrValue(@PathVariable("spuId") Long spuId);


//    =========================  商品详情页需要的接口

    //根据skuId查询skuInfoEntity的方法
    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> info(@PathVariable("skuId") Long skuId);

    //根据skuId查询sku图片的方法
    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<String>> querySkuImagesBySkuId(@PathVariable("skuId")Long skuId);

    //根据spuId查询所有的销售属性
    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySaleAttrBySpuId(@PathVariable("spuId")Long spuId);

    //  ===============================  购物车需要的接口
    @GetMapping("pms/skusaleattrvalue/query/{skuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrBySkuId(@PathVariable("skuId")Long skuId);
}
