package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SkuSaleAttrValueDao;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;

import java.util.List;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageVo(page);
    }

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    /**
     * 根据spuId查询出spuId对应的所有的销售属性
     * @param spuId
     * @return
     */
    @Override
    public List<SkuSaleAttrValueEntity> querySaleAttrBySpuId(Long spuId) {

        //1.根据spuId查询出所有的skuInfoEntity
        List<SkuInfoEntity> skuInfoEntities = this.skuInfoService.querySkuBySpuId(spuId);

        //2.根据skuInfoEntity查询出所有符合条件的skuId
        List<Long> skuIds = skuInfoEntities.stream().map(skuInfoEntity -> {
            Long skuId = skuInfoEntity.getSkuId();
            return skuId;
        }).collect(Collectors.toList());

        //3.根据符合条件的skuId查询出所有对应属性的集合
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = this.skuSaleAttrValueService.list(new QueryWrapper<SkuSaleAttrValueEntity>().in("sku_id", skuIds));

        return skuSaleAttrValueEntities;
    }

    @Override
    public List<SkuSaleAttrValueEntity> querySkuSaleAttrBySkuId(Long skuId) {
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = this.skuSaleAttrValueService.list(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", skuId));
        return skuSaleAttrValueEntityList;
    }

}