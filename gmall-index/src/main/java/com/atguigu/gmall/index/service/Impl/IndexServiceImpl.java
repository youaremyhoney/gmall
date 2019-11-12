package com.atguigu.gmall.index.service.Impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sun.security.provider.PolicySpiFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

    //定义缓存的前缀
    private static final String CATEGORY_CACHE_KEY_PREFIX = "index:category:";

    @Autowired
    private GmallPmsFeign pmsFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Resp<List<CategoryEntity>> queryLevel1Category() {
        Resp<List<CategoryEntity>> category = this.pmsFeign.queryCategory(1, 0l);
        List<CategoryEntity> categoryEntities = category.getData();
        return Resp.ok(categoryEntities);
    }

    @GmallCache(prefix = CATEGORY_CACHE_KEY_PREFIX)
    @Override
    public List<CategoryEntity> querySubCategory(Long pid) {

        /*//从缓存中获取
        String cache = this.redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY_PREFIX + pid);

        //如果缓存中有，直接返回
        if (!StringUtils.isEmpty(cache)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(cache,CategoryEntity.class);
            return categoryEntities;
        }*/

        //如果没有调用远程接口去查询数据
        List<CategoryEntity> categoryEntities = new ArrayList<>();

        Resp<List<CategoryEntity>> queryCategory = this.pmsFeign.queryCategory(2, pid);
        List<CategoryEntity> data = queryCategory.getData();
        //先将二级分类的属性复制给vo对象
        for (CategoryEntity entity : data) {
            CategoryEntity categoryEntity = new CategoryEntity();
            BeanUtils.copyProperties(entity,categoryEntity);
            Long catId = entity.getCatId();
            Resp<List<CategoryEntity>> listResp = this.pmsFeign.queryCategory(3, catId);
            List<CategoryEntity> data1 = listResp.getData();
            //将三级分类复制给vo对象
            categoryEntity.setSubs(data1);
            categoryEntities.add(categoryEntity);
        }

        /*//最后将数据放入缓存
        this.redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY_PREFIX + pid ,JSON.toJSONString(categoryEntities));*/

        return categoryEntities;
    }

    //测试分布式锁
    @Override
    public   void testLock() {
        // 1. 从redis中获取锁,setnx
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", "111");
        if (lock) {
            // 查询redis中的num值
            String value = this.redisTemplate.opsForValue().get("num");
            // 没有该值return
            if (StringUtils.isEmpty(value)){
                return ;
            }
            // 有值就转成成int
            int num = Integer.parseInt(value);
            // 把redis中的num值+1
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            // 2. 释放锁 del
            this.redisTemplate.delete("lock");
        } else {
            // 3. 每隔1秒钟回调一次，再次尝试获取锁
            try {
                Thread.sleep(1000);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
