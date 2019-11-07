package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author liuziqiang
 * @email 525409941@qq.com
 * @date 2019-10-28 20:38:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
