package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.VO.AttrVO;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    /**
     * 查询规格参数的实现方法
     * @param catelogId
     * @param attrType
     * @param condition
     * @return
     */
    @Override
    public PageVo queryByCidTypePage(Long catelogId, Integer attrType, QueryCondition condition) {
        //构建查询条件
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id",catelogId);
        //如果查询基本属性还是销售属性的条件不为空，加上条件，否则，查询所有
        if (attrType != null) {
            wrapper.eq("attr_type",attrType);
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(condition), wrapper);
        return new PageVo(page);
    }

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public void saveAttrVO(AttrVO attrVO) {
        //先将attrVO所有和attr相同的参数新增进去
        this.attrDao.insert(attrVO);

        //新增中间表
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attrVO.getAttrId());
        relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        this.relationDao.insert(relationEntity);
    }

}