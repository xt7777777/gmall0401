package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.SkuLsInfo;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;

/**
 * @author xtsky
 * @create 2019-09-16 19:57
 */
public interface ListService {

    public void saveSkuLsInfo (SkuLsInfo skuLsInfo);

    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParam);

    public void incrHotScore(String skuId);

}
