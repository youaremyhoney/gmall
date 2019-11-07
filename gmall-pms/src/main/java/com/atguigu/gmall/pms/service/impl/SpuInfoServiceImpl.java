package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.client.naming.utils.StringUtils;
import com.atguigu.gmall.pms.VO.ProductAttrValueVO;
import com.atguigu.gmall.pms.VO.SkuInfoVO;
import com.atguigu.gmall.pms.VO.SpuInfoVO;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfo(QueryCondition condition, Long catId) {
        //封装分页条件
        IPage<SpuInfoEntity> page = new Query<SpuInfoEntity>().getPage(condition);

        //因为condition里面还包含了一个key参数，所以可能会出现两种情况
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (catId != 0){
            wrapper.eq("catalog_id",catId);
        }

        String key = condition.getKey();
        if (!StringUtils.isEmpty(key)){
            wrapper.and(t -> t.like("spu_name",key).or().like("id",key));
        }

        return new PageVo(this.page(page,wrapper));
    }

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    //注入远程调用需要的类
    @Autowired
    private GmallSmsClient gmallSmsClient;

    /**
     * 因为数据比较复杂，所以我们按照三步完成上述的操作 -- 因为需要同时操作9张表
     * @param spuInfoVO
     */
    @Override
    public void saveSpuInfoVO(SpuInfoVO spuInfoVO) {
        //1、保存spu相关的信息
            //1.1 先保存基本信息 spu_info
        spuInfoVO.setPublishStatus(1);//默认是已经上架
        spuInfoVO.setCreateTime(new Date());//设置创建时间为当前时间
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());//新增的时候两个时间一致
            //因为需要拿到spu_id所以需要自己手动提交
        this.save(spuInfoVO);
        Long spuId = spuInfoVO.getId();

            //1.2 保存spu的描述信息(图片描述信息) spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);//因为主键不是自增的，所以需要手动设置上去
        //将描述的图片信息变成一串字符串，然后以逗号分隔，保存到spuInfoDescEntity中
        spuInfoDescEntity.setDecript(StringUtils.join(spuInfoVO.getSpuImages(),","));
        //将保存到spuInfoDescEntity的信息插入数据库
        this.spuInfoDescDao.insert(spuInfoDescEntity);

            //1.3 保存spu规格参数信息 -- 还差attr_sort、spu_id、quick_show字段
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntity = baseAttrs.stream().map(asq -> {
            asq.setSpuId(spuId);
            asq.setAttrSort(0);
            asq.setQuickShow(0);
            return asq;
        }).collect(Collectors.toList());
        //一次性插入批量数据
        this.productAttrValueService.saveBatch(productAttrValueEntity);

        //2、保存sku相关的信息
        List<SkuInfoVO> skuInfoVOS = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skuInfoVOS)){
            return ;
        }
            //2.1 保存sku基本信息
        skuInfoVOS.forEach(skuInfoVO -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            //将传入参数里面有的字段复制给skuInfoEntiry对象
            BeanUtils.copyProperties(skuInfoVO,skuInfoEntity);
            //因为传入参数里面还有两个字段没有(brand_id、catelog_id)，所以需要从spuInfoVO里面获取
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            //随机生成一串唯一字符串用于sku_code编码 -- touppercase()是将生成的随机唯一码转换成大写
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0,10).toUpperCase());
            //获取图片列表
            List<String> images = skuInfoVO.getImages();//skuinfoimages
            if (!CollectionUtils.isEmpty(images)){
                //如果图片列表不为空，将第一张图片设置为默认图片
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg() == null ? images.get(0) : skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSpuId(spuId);
            //将数据保存到skuinfo表中，方便获取sku_id
            this.skuInfoDao.insert(skuInfoEntity);
            //获取sku_id
            Long skuId = skuInfoEntity.getSkuId();

        //2.2 保存sku图片信息
            List<String> skuInfoVOImages = skuInfoVO.getImages();
            //获得默认图片用来和每一张图片比较，确认图片是否是默认图片
            String defaultImage = skuInfoVOImages.get(0);
            //如果图片不为空，判断图片是否为默认图片
            if (!CollectionUtils.isEmpty(skuInfoVOImages)){
                List<SkuImagesEntity> skuImagesEntities = skuInfoVOImages.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultImg(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setImgSort(0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }

            //2.3 保存sku销售属性信息
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            saleAttrs.forEach(saleAttr -> {
                saleAttr.setAttrName(this.attrDao.selectById(saleAttr.getAttrId()).getAttrName());
                saleAttr.setAttrSort(0);
                saleAttr.setSkuId(skuId);
            });
            this.skuSaleAttrValueService.saveBatch(saleAttrs);


        //3、保存营销相关的信息 ， 新增营销信息的三张表 skuId
            //3.1 新增积分 sms_sku_bounds

            //3.2 新增打折信息 sms_sku_ladder

            //3.3 新增满减信息 sms_sku_full_reduction

        //一个远程调用直接完成上面三个功能
        SaleVO saleVO = new SaleVO();
        BeanUtils.copyProperties(skuInfoVO,saleVO);
        saleVO.setSkuId(skuId);
        this.gmallSmsClient.saveSale(saleVO);
        });

    }

}