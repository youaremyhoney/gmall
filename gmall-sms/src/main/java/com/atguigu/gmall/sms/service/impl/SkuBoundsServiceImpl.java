package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Autowired
    private SkuLadderDao skuLadderDao;

    @Autowired
    private SkuFullReductionDao skuFullReductionDao;

    @Override
    public void saveSale(SaleVO saleVO) {
        //3.1 新增积分 sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setGrowBounds(saleVO.getGrowBounds());
        skuBoundsEntity.setBuyBounds(saleVO.getBuyBounds());
        List<Integer> works = saleVO.getWork();
        if (!CollectionUtils.isEmpty(works) && works.size() == 4){
        skuBoundsEntity.setWork(works.get(0) * 8 + works.get(1) * 4 + works.get(2) * 2 + works.get(3) * 1);
        skuBoundsEntity.setSkuId(saleVO.getSkuId());
        }
        this.save(skuBoundsEntity);
        System.out.println(skuBoundsEntity.toString());

        //3.2 新增打折信息 sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setFullCount(saleVO.getFullCount());
        skuLadderEntity.setDiscount(saleVO.getDiscount());
        skuLadderEntity.setAddOther(saleVO.getLadderAddOther());
        skuLadderEntity.setSkuId(saleVO.getSkuId());
        this.skuLadderDao.insert(skuLadderEntity);

        //3.3 新增满减信息 sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setFullPrice(saleVO.getFullPrice());
        skuFullReductionEntity.setReducePrice(saleVO.getReducePrice());
        skuFullReductionEntity.setAddOther(saleVO.getFullAddOther());
        skuFullReductionEntity.setSkuId(saleVO.getSkuId());
        this.skuFullReductionDao.insert(skuFullReductionEntity);
        System.out.println(skuFullReductionEntity);
    }

    @Autowired
    private SkuBoundsDao skuBoundsDao;

    //根据skuId查询所有的优惠信息(成长积分、购物积分、满减、打折)
    @Override
    public List<ItemSaleVO> queryItemSaleVOS(Long skuId) {
        List<ItemSaleVO> itemSaleVOS = new ArrayList<>();


        //查询满减信息
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        BigDecimal fullPrice = skuFullReductionEntity.getFullPrice();
        BigDecimal reducePrice = skuFullReductionEntity.getReducePrice();
        ItemSaleVO itemSaleVO = new ItemSaleVO();
        itemSaleVO.setType("满减");
        itemSaleVO.setDesc("满" + fullPrice + "元减" + reducePrice +"元");
        itemSaleVOS.add(itemSaleVO);

        //查询打折信息
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        Integer fullCount = skuLadderEntity.getFullCount();
        BigDecimal discount = skuLadderEntity.getDiscount();
        ItemSaleVO itemSaleVO1 = new ItemSaleVO();
        itemSaleVO1.setType("打折");
        itemSaleVO1.setDesc("满" + fullCount + "件打" + discount + "折");
        itemSaleVOS.add(itemSaleVO1);

        //查询积分信息
            //成长积分
            SkuBoundsEntity skuBoundsEntity = this.skuBoundsDao.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
            BigDecimal growBounds = skuBoundsEntity.getGrowBounds();
            ItemSaleVO itemSaleVO2 = new ItemSaleVO();
            itemSaleVO2.setType("成长积分");
            itemSaleVO2.setDesc("赠送" + growBounds + "成长积分");
            itemSaleVOS.add(itemSaleVO2);

            //购买积分
            BigDecimal buyBounds = skuBoundsEntity.getBuyBounds();
            ItemSaleVO itemSaleVO3 = new ItemSaleVO();
            itemSaleVO3.setType("购买积分");
            itemSaleVO3.setDesc("赠送" + buyBounds + "成长积分");
            itemSaleVOS.add(itemSaleVO3);



        return itemSaleVOS;
    }

}