package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class GmallSearchApplicationTests {


    @Autowired
    private JestClient jestClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    public void importData(){

        Long pageNum = 1l;
        Long pageSize = 100l;

        do {

            QueryCondition condition = new QueryCondition();
            condition.setPage(pageNum);
            condition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp1 = this.pmsClient.querySpuPage(condition);
            List<SpuInfoEntity> spuInfoEntities = listResp1.getData();


                //遍历下面的所有sku信息，导入到索引库中
            for (SpuInfoEntity spuInfoEntity : spuInfoEntities) {
                Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (CollectionUtils.isEmpty(skuInfoEntities)){
                    continue;
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
                    Resp<List<SpuAttributeValueVO>> listResp = this.pmsClient.querySearchAttrValue(skuInfoEntity.getSpuId());
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

            }

            pageSize =  Long.valueOf(spuInfoEntities.size() );
            pageNum++;
        }while (pageSize == 100);
    }














}
