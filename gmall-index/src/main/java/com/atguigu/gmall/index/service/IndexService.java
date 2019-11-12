package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {
    Resp<List<CategoryEntity>> queryLevel1Category();

    List<CategoryEntity> querySubCategory(Long pid);

    void testLock();

}
