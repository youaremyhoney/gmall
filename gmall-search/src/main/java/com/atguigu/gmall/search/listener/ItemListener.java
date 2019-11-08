package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class ItemListener {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL-SERVICE-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.*"}
    ))
    public void listener(Map<String,Object> map){



        if (CollectionUtils.isEmpty(map)){
            return;
        }
        Long spuId = (Long)map.get("id");
        String type = map.get("type").toString();

        if (StringUtils.equals("insert",type) || StringUtils.equals("update",type)){
            Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)){
                return;
            }

            skuInfoEntities.forEach(skuInfoEntity -> {
                GoodsVO goodsVO = new GoodsVO();

                //设置和sku相关的信息
                goodsVO.setName(skuInfoEntity.getSkuTitle());
                goodsVO.setId(skuInfoEntity.getSkuId());
                goodsVO.setPrice(skuInfoEntity.getPrice());
                goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());
                goodsVO.setSale(100);//设置默认销量为100
                goodsVO.setSort(0);//设置默认排序为0

                //设置和品牌相关的信息
                Resp<BrandEntity> brandEntityResp = this.pmsClient.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brandInfo = brandEntityResp.getData();
                if (brandInfo != null){
                    goodsVO.setBrandId(skuInfoEntity.getBrandId());
                    goodsVO.setBrandName(brandInfo.getName());
                }

                //设置和分类相关的信息
                Resp<CategoryEntity> categoryEntityResp = this.pmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryInfo = categoryEntityResp.getData();
                if (categoryInfo != null){
                    goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                    goodsVO.setProductCategoryName(categoryInfo.getName());
                }

                //设置和检索信息相关的信息
                Resp<List<SpuAttributeValueVO>> listResp = this.pmsClient.querySearchAttrValue(spuId);
                List<SpuAttributeValueVO> spuAttributeValueVOS = listResp.getData();
                goodsVO.setAttrValueList(spuAttributeValueVOS);

                //库存
                Resp<List<WareSkuEntity>> stockInfo = this.wmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = stockInfo.getData();
                if (wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0)) {
                    goodsVO.setStock(1l);
                } else {
                    goodsVO.setStock(0l);
                }

                Index index = new Index.Builder(goodsVO).index("goods").type("info").id(String.valueOf(skuInfoEntity.getSkuId())).build();
                try {
                    this.jestClient.execute(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }else if (StringUtils.equals("delete",type)){
            Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)){
                return;
            }

            skuInfoEntities.forEach(skuInfoEntity -> {
                Delete delete = new Delete.Builder(skuInfoEntity.getSkuId().toString()).index("goods").type("info").build();
                try {
                    this.jestClient.execute(delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
