package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public ItemVO loadItem(Long skuId) {

        ItemVO itemVO = new ItemVO();

        CompletableFuture<SkuInfoEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            //1.获取sku的基本信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.info(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                //如果查询出来的sku信息不为空，那么将属性直接复制给itemVO
                BeanUtils.copyProperties(skuInfoEntity, itemVO);
            }
            return skuInfoEntity;
        }, executor);


        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            //2.获取sku图片信息
            Resp<List<String>> imagesResp = this.pmsClient.querySkuImagesBySkuId(skuId);
            List<String> pics = imagesResp.getData();
            if (!CollectionUtils.isEmpty(pics)) {
                itemVO.setPics(pics);
            }
        }, executor);

        //3.获取sku的促销信息

        //4.获取所有spu的销售属性
        Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueEntities = this.pmsClient.querySaleAttrBySpuId(21l);
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = skuSaleAttrValueEntities.getData();
        itemVO.setSaleAttrs(skuSaleAttrValueEntityList);

        //5.获取规格参数组及组下的规格参数

        CompletableFuture<Void> future = CompletableFuture.allOf(skuImageFuture, skuFuture);

        //阻塞主线程，等待子线程全部执行完毕 -- 可以使用join()或者get()阻塞线程，好处就是join()不需要处理异常
        future.join();

        return itemVO;
    }
}
