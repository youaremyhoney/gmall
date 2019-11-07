package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.VO.AttrGroupVO;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByIdCidPage(Long catId, QueryCondition condition) {
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(condition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
        return new PageVo(page);
    }

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Override
    public AttrGroupVO queryByID(Long gid) {

        //创建一个AttrGroupVO来接收所有查询到的信息
        AttrGroupVO attrGroupVO = new AttrGroupVO();

        //1、根据cid查询所在的分组的信息
        AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(gid);//查询到所属组信息
        BeanUtils.copyProperties(attrGroupEntity,attrGroupVO);//将查询到的分组信息复制给AttrGroupVO

        //2、根据所查询到的分组信息，查询分组和规格参数的关系表
        List<AttrAttrgroupRelationEntity> relations = this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        //需要判断查询到的关系表信息是否为空
        if (CollectionUtils.isEmpty(relations)) {
            return attrGroupVO;
        }
        //将查询到的分组信息和规格属性信息的关系表设置给AttrGroupVO
        attrGroupVO.setRelations(relations);


        //先收集分组关系下面所有的attr_id
        List<Long> attrIds = relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());

        //3、根据说查询到的关系表的信息利用字段attr_id，去插叙你需要的规格信息
        List<AttrEntity> attrEntities = this.attrDao.selectList(new QueryWrapper<AttrEntity>().in("attr_id",attrIds));
        attrGroupVO.setAttrEntities(attrEntities);

        return attrGroupVO;
    }

    @Override
    public List<AttrGroupVO> queryByCid(Long catId) {

        List<AttrGroupEntity> attrGroupEntities = this.attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));

        List<AttrGroupVO> groupVOS = attrGroupEntities.stream().map(attr -> {
            return this.queryByID(attr.getAttrGroupId());
        }).collect(Collectors.toList());
        return groupVOS;
    }

}