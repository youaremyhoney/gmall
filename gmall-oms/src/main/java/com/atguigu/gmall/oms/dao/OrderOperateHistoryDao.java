package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderOperateHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单操作历史记录
 * 
 * @author liuziqiang
 * @email 525409941@qq.com
 * @date 2019-10-28 20:26:14
 */
@Mapper
public interface OrderOperateHistoryDao extends BaseMapper<OrderOperateHistoryEntity> {
	
}
