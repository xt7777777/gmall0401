package com.atguigu.gmall0401.gmall0401manageservice.mapper;

import com.atguigu.gmall0401.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-05 22:31
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    public List<BaseAttrInfo> getBaseAttrInfoListByValueIds(@Param("valueIds") String valueIds);

}

