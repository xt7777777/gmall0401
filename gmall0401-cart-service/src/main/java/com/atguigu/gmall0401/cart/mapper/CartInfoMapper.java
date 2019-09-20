package com.atguigu.gmall0401.cart.mapper;

import com.atguigu.gmall0401.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-20 19:49
 */
public interface CartInfoMapper extends Mapper<CartInfo> {

    public List<CartInfo> selectCartListWithSkuPrice(String userId);

}
