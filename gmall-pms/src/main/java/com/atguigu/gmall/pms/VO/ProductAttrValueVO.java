package com.atguigu.gmall.pms.VO;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ProductAttrValueVO extends ProductAttrValueEntity {

    //因为这个只是字段名不一样，那么可以直接调用set方法将这个字段的值设置上去
    private void setValueSelected(List<Object> valueSelected){
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        //stringutils工具类的join方法，就是将一个集合转换成一个字符串，并且在这中间以指定的分隔符分开
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }

}
