package com.atguigu.gmall0401.gmall0401manageservice.mapper;

import com.atguigu.gmall0401.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-08 23:40
 */
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId);

    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdCheckSku(@Param("spuId") String spuId,@Param("skuId") String skuId);

}
