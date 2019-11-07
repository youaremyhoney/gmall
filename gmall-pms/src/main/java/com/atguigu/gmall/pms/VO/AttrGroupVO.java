package com.atguigu.gmall.pms.VO;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * 这个类继承AttrGroupEntiry，然后加上两个字段来作为查询组和组的规格参数的响应结果封装类
 */
@Data
public class AttrGroupVO extends AttrGroupEntity {
    private List<AttrEntity> attrEntities;

    private List<AttrAttrgroupRelationEntity> relations;
}
