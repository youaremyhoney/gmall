package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    //查询一级分类的方法
    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryLevel1Category(){
        return indexService.queryLevel1Category();
    }

    //查询一级分类的二三级分类的方法 -- 因为响应的字段中需要有一个subs字段，所以需要给categoryEntity扩展
    @GetMapping("cates/{pid}")
    public Resp<List<CategoryEntity>> querySubCategory(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryVOS = this.indexService.querySubCategory(pid);
        return Resp.ok(categoryVOS);
    }

    //测试分布式锁
    @GetMapping("testlock")
    public Resp<Object> testLock(){
        indexService.testLock();

        return Resp.ok("测试完成");
    }

}
