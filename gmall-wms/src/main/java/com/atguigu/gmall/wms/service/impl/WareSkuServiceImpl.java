package com.atguigu.gmall.wms.service.impl;

import com.atguigu.gmall.wms.dao.WareInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private WareInfoDao wareInfoDao;

    @Override
    public List<WareSkuEntity> queryWareSkuBySkuId(Long skuId) {
        //先查询出skuId和仓库信息的关联表的信息
        List<WareSkuEntity> wareSkuEntityList = this.wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));

        return wareSkuEntityList;
    }

}