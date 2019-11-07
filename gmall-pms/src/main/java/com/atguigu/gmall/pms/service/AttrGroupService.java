package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.VO.AttrGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author liuziqiang
 * @email 525409941@qq.com
 * @date 2019-10-28 20:20:32
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryByIdCidPage(Long catId, QueryCondition condition);

    AttrGroupVO queryByID(Long gid);

    List<AttrGroupVO> queryByCid(Long catId);
}

