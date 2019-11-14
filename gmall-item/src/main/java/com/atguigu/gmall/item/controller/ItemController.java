package com.atguigu.gmall.item.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 根据skuId查询商品详情页的信息
     * @param skuId
     * @return 返回的是自己封装的一个对象ItemVO
     */
    @GetMapping("/item/{skuId}")
    public Resp<ItemVO> loadItem(@PathVariable("skuId") Long skuId){

        ItemVO itemVO = this.itemService.loadItem(skuId);

        return Resp.ok(itemVO);
    }

}
