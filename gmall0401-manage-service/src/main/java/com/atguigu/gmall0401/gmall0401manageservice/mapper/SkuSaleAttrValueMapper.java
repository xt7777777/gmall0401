package com.atguigu.gmall0401.gmall0401manageservice.mapper;

import com.atguigu.gmall0401.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-09 20:52
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    public List<Map> getSaleAttrValuesBySpu(String spuId);

}
