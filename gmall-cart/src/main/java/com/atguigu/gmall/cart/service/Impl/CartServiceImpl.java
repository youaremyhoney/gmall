package com.atguigu.gmall.cart.service.Impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    public static final String CART_PREFIX = "cat:uid:";

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //根据用户页面传过来的skuId和count将商品详情添加到用户的购物车
    @Override
    public void addCart(Cart cart) {

        String cartJSON = null;

        Integer count = cart.getCount(); // 获取用户传过来的商品的数量
        Long skuId = cart.getSkuId(); // 获取用户想添加到购物车的商品的skuId

        //获取userInfo
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        String key = CART_PREFIX;
        if (userInfo.getUserId() == null){
            //用户没有登陆
            key += userInfo.getUserKey();
        }else {
            //如果用户已经登陆
            key += userInfo.getUserId();
        }

        //查询用户购物车
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())){ // 说明用户的购物车中有这个商品
            //更新数量
            String cartJson = hashOps.get(skuId.toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);
        }else {
            //说明这个购物车中没有这个商品 -- 新增商品
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.info(skuId);
            SkuInfoEntity skuinfoentity = skuInfoEntityResp.getData();
            cart.setCount(count);
            cart.setCheck(true);
            cart.setDefaultImage(skuinfoentity.getSkuDefaultImg());
            cart.setPrice(skuinfoentity.getPrice());
            cart.setSkuId(skuId);
            cart.setTitle(skuinfoentity.getSkuTitle());
            Resp<List<SkuSaleAttrValueEntity>> listResp = this.pmsClient.querySkuSaleAttrBySkuId(skuId);
            cart.setSkuAttrValue(listResp.getData());
            //设置优惠信息到cart
            Resp<List<ItemSaleVO>> queryItemSaleVOS = this.smsClient.queryItemSaleVOS(skuId);
            List<ItemSaleVO> itemSaleVOS = queryItemSaleVOS.getData();
            cart.setSales(itemSaleVOS);
            cartJSON = JSON.toJSONString(cart);
        }

        //最后将购物车信息，写入redis
        hashOps.put(skuId.toString(),cartJSON);


    }

    //查询用户车的方法
    @Override
    public List<Cart> queryCart() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询未登录的购物车
        List<Cart> userKeyCarts = null;
        String userKey = CART_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> userKeyOps = this.stringRedisTemplate.boundHashOps(userKey);
        List<Object> cartJsonList = userKeyOps.values();
        if (!CollectionUtils.isEmpty(cartJsonList)){
            userKeyCarts = cartJsonList.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).collect(Collectors.toList());
        }

        //判断用户是否登陆，未登录直接返回
        if (userInfo.getUserId() == null){
            return userKeyCarts;
        }

        //如果用户已经登陆，查询用户已经登陆的购物车
        String key = CART_PREFIX + userInfo.getUserId();
        //获取登陆状态的购物车操作对象
        BoundHashOperations<String, Object, Object> userIdOps = this.stringRedisTemplate.boundHashOps(key);

        if (!CollectionUtils.isEmpty(userKeyCarts)) {//如果未登录状态购物车不为空，需要合并购物车
            //合并购物车
            userKeyCarts.forEach(userKeyCart -> {
                Long skuId = userKeyCart.getSkuId();
                Integer count = userKeyCart.getCount();
                //如果登陆购物车里面有这条记录，那么直接添加数量
                if (userIdOps.hasKey(skuId.toString())){
                    String cartJson = userIdOps.get(skuId.toString()).toString();//用户已经存在的记录的json对象
                    Cart cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount() + count);
                }
                //如果登陆购物车里面没有这条记录，那么新增一条记录
                userIdOps.put(skuId.toString(), JSON.toJSONString(userKeyCart));
            });
            //合并完成后，删除未登录状态的购物车
            this.stringRedisTemplate.delete(userKey);
        }

        //返回登陆状态的购物车
        List<Object> userCartJsonList = userIdOps.values();
        if (!CollectionUtils.isEmpty(userCartJsonList)) {
            return userCartJsonList.stream().map(userCartJson -> JSON.parseObject(userCartJson.toString(),Cart.class)).collect(Collectors.toList());
        }

        return null;
    }
}
